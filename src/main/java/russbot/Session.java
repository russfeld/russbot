/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot;

import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;
import russbot.plugins.Plugin;

/**
 * Session class for Russbot
 * Where most of the action actually happens
 * Uses the singleton pattern to maintain a global copy of the class internally
 *
 * @author russfeld
 */
public final class Session {
    //singleton session variable
    private static Session session;

    //singleton Slack session
    private SlackSession slacksession;

    //list of plugins loaded to Russbot
    private List<PluginContainer> plugins;

    //List of private channels created by the system thus far
    private HashMap<String, Plugin> privateChannels;

    //List of channels allowed by the configuration
    private List<String> allowedChannels;

    //Boolean to load test mode
    private static boolean test = false;

    /**
     * Constructor for Session - blank at this time
     */
    private Session(){

    }

    /**
     * Singleton implementation of getInstance
     * ALWAYS call getInstance to interact with the Session - it will guarantee consistency
     *
     * @return Session - singleton instance of this class
     */
    public static Session getInstance(){
        //if session is not loaded, load one now
        if(session == null){
            session = new Session();
            session.plugins = new LinkedList<>();
            session.privateChannels = new HashMap<>();
            session.allowedChannels = Arrays.asList(getProperty("channels").split(","));
        }

        //if Slack is not connected, load the Slack web socket for connection
        if(session.slacksession == null){
            session.slacksession = SlackSessionFactory.createWebSocketSlackSession(getProperty("token"));
        }

        //return the singleton Session instance
        return session;
    }

    /**
     * Method to connect to Slack and register all listeners
     *
     * @param test - boolean for test mode
     */
    public void connect(boolean test){

        //check config file for test parameter as well
        if(!test){
            test = Boolean.parseBoolean("test");
        }

        //if loading in test mode, read input from command line
        if(test){
            this.test = true;
            InputListener input = new InputListener(new MessagePostedListener());
            System.out.println("TESTING MODE!");
            System.out.println("To change channel, type # followed by a channel name (like #random)");
            System.out.println("To change user, type @ followed by a user name (like @user)");
            input.run();

        //if running for real, connect to Slack
        }else {
            try {
                session.slacksession.addMessagePostedListener(new MessagePostedListener());
                session.slacksession.connect();
            } catch (IOException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(3);
            }
        }
    }

    /**
     * Add a plugin to the list of plugins
     * @param p - Plugin to be added
     */
    public void addPlugin(Plugin p){
        plugins.add(new PluginContainer(p));
        Logger.getLogger(Session.class.getName()).log(Level.INFO, "Plugin " + p.getClass().getCanonicalName() + " registered");
    }

    /**
     * Register a private channel with the system - assigns it a name so it can be referenced later
     *
     * @param usernames - List of users to connect with
     * @param plugin - The plugin requesting the channel (to prevent collisions)
     * @return String - the channel name
     */
    public String registerPrivateChannel(String[] usernames, Plugin plugin){
        //If testing mode, simply create a dummy channel name and return
        if(test){
            sendMessage("```Created private channel for users " + Arrays.toString(usernames) + " to plugin " + plugin.getClass().getName() + "```", "terminal");
            sendMessage("```Use #terminal-" + Arrays.toString(usernames) + " to connect```", "terminal");
            privateChannels.put("terminal-" + Arrays.toString(usernames), plugin);
            return "terminal-" + Arrays.toString(usernames);

        //if in real mode, create the channel through Slack
        }else {
            String channel = null;

            //if multiple usernames are used
            if (usernames.length > 1) {

                //Get a list of SlackUsers for those users
                LinkedList<SlackUser> users = new LinkedList<SlackUser>();
                for (String username : usernames) {
                    users.add(session.slacksession.findUserByUserName(username));
                }

                //Get a message handle to talk to all of those users
                SlackMessageHandle<SlackChannelReply> reply = session.slacksession.openMultipartyDirectMessageChannel(users.toArray(new SlackUser[]{}));
                channel = reply.getReply().getSlackChannel().getId();

            //if a single username is provided
            } else {

                //Get that user's SlackUser and create a direct message channel
                SlackUser user = session.slacksession.findUserByUserName(usernames[0]);
                SlackMessageHandle<SlackChannelReply> reply = session.slacksession.openDirectMessageChannel(user);
                channel = reply.getReply().getSlackChannel().getId();
            }

            //if we weren't able to load the channel, throw an error
            if (channel == null) {
                Logger.getLogger(Session.class.getName()).log(Level.INFO, "Unable to register private channel for " + plugin.getClass().getName());
                return null;
            }

            //if that channel already exists, close it before loading a new one
            if (privateChannels.containsKey(channel)) {
                session.sendPrivateMessage("```Disconnecting from " + privateChannels.get(channel).getClass().getName() + "...```", channel);
            }

            //store the channel and return the name back to the plugin
            privateChannels.put(channel, plugin);
            session.sendPrivateMessage("```You are now connected to " + plugin.getClass().getName() + "...```", channel);
            return channel;
        }
    }

    /**
     * Unregisters a previously created private channel
     *
     * @param channel - the channel name to unregister
     */
    public void unregisterPrivateChannel(String channel){
        if(privateChannels.containsKey(channel)){
            session.sendPrivateMessage("```Disconnecting from " + privateChannels.get(channel).getClass().getName() + "...```", channel);
            privateChannels.remove(channel);
        }
    }

    /**
     * Shorthand method for sending a Slack message
     *
     * @param message - message to send
     * @param channel - channel to send it to
     */
    public void sendMessage(String message, String channel){
        sendMessage(message, channel, null);
    }

    /**
     * Actual method for sending a Slack message to a channel
     *
     * @param message - message to send
     * @param channel - channel to send it to
     * @param attachment - an attachment if included, or null if not
     */
    public void sendMessage(String message, String channel, SlackAttachment attachment){
        if(test){
            System.out.println("#" + channel + " > " + message);
            if(attachment != null){
                System.out.println("Attachment (best tested in Slack itself): " + attachment.getText());
            }
        }else {
            session.slacksession.sendMessage(session.slacksession.findChannelByName(channel), message, attachment);
        }
    }

    /**
     * Shorthand method for sending a direct message to a user
     *
     * @param message - message to send
     * @param user - user to send it to
     */
    public void sendDirectMessage(String message, String user){
        sendDirectMessage(message, user, null);
    }

    /**
     * Actual method for sending a direct message to a user
     *
     * @param message - message to send
     * @param user - user to send it to
     * @param attachment - an attachment if included, or null if not
     */
    public void sendDirectMessage(String message, String user,  SlackAttachment attachment){
        if(test){
            System.out.println("DM: @" + user + " > " + message);
            if(attachment != null){
                System.out.println("Attachment (best tested in Slack itself): " + attachment.getText());
            }
        }else {
            session.slacksession.sendMessageToUser(user, message, attachment);
        }
    }

    /**
     * Shorthand method for sending a message to a private channel
     *
     * @param message - message to send
     * @param channel - channel to send it to
     */
    public void sendPrivateMessage(String message, String channel){
        sendPrivateMessage(message, channel, null);
    }

    /**
     * Actual method for sending a message to a private channel
     *
     * @param message - message to send
     * @param channel - channel to send it to
     * @param attachment - an attachment if included, or null if not
     */
    public void sendPrivateMessage(String message, String channel, SlackAttachment attachment){
        if(test){
            System.out.println("#" + channel + " > " + message);
            if(attachment != null){
                System.out.println("Attachment (best tested in Slack itself): " + attachment.getText());
            }
        }else {
            session.slacksession.sendMessage(session.slacksession.findChannelById(channel), message, attachment);
        }
    }

    /**
     * Method to read properties from the configuration file
     *
     * @param keyName - name of the key to be read
     * @return the String value of the key
     */
    public static String getProperty(String keyName){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("russbot.cfg"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(2);
        }
        return properties.getProperty(keyName).toString();
    }

    /**
     * Internal class to represent a Plugin in the list of plugins
     */
    private class PluginContainer{
        //regex pattern that triggers the plugin
        public Pattern pattern;
        //channels it is registered to listen on
        public HashSet channels;
        //the Plugin itself
        public Plugin plugin;

        /**
         * Constructor for a PluginContainer object
         *
         * @param p - the Plugin
         */
        public PluginContainer(Plugin p){
            pattern = Pattern.compile(p.getRegexPattern());
            channels = new HashSet();
            for(String s : p.getChannels()){
                if(allowedChannels.contains(s)) {
                    channels.add(s);
                }
            }
            plugin = p;
        }
    }

    /**
     * Internal class to represent a message listener to handle incoming messages
     */
    private class MessagePostedListener implements SlackMessagePostedListener{

        /**
         * This method is called by Slack whenever a message is posted in Slack where Russbot can see it
         *
         * @param event - the SlackMessagePosted event received
         * @param ss - the SlackSession it came from
         */
        @Override
        public void onEvent(SlackMessagePosted event, SlackSession ss) {
            String channel = event.getChannel().getName();
            String message = StringEscapeUtils.unescapeHtml4(event.getMessageContent());
            String username = event.getSender().getUserName();
            String userid = event.getSender().getId();

            //call russbot's handleMessage function with the given information
            handleMessage(channel, message, username, userid);
        }

        /**
         * This methos is called by the testing framework whenever a message is received via the command line
         *
         * @param input - the message entered
         * @param channel - the channel the user is currently on
         * @param user - the user who sent the message
         */
        public void onTestEvent(String input, String channel, String user){
            handleMessage(channel, input, user, "U1234ABCD");
        }

        /**
         * This method handles all incoming messages by parsing the message and sending it to the various plugins
         * as requested
         *
         * @param channel - the channel the message was received on
         * @param message - the message itself
         * @param username - the username who sent the message
         * @param userid - the Slack userID of that user
         */
        private void handleMessage(String channel, String message, String username, String userid){
            //only accept messages on allowed channels or created private channels
            if(allowedChannels.contains(channel) || privateChannels.containsKey(channel)) {

                //if the message is asking for help, handle the help part of the system
                if (message.equals("!help") || message.equals("!commands") || message.equals("!about")) {
                    handleHelp(channel, message, username, userid);

                //handle messages normally
                } else {

                    //debugging only
                    //System.out.println(channel + ":" + message + ":" + username + ":" + userid);

                    //iterate through plugins
                    for (PluginContainer pc : session.plugins) {
                        if (pc.channels.contains(channel)) {
                            if (pc.pattern.matcher(message).matches()) {
                                pc.plugin.messagePosted(message, channel, username, userid);
                            }
                        }
                    }

                    //check if message is sent via a private channel
                    if (privateChannels.containsKey(channel) && !username.equals("russbot")) {
                        privateChannels.get(channel).privateMessagePosted(message, channel, username, userid);
                    }
                }
            }
        }

        /**
         * This method sends the help information about the current plugins to the user requesting help
         *
         * @param channel - the channel the user is currently asking from
         * @param username - the username of the user
         */
        private void handleHelp(String channel, String message, String username, String userid){
            //if the help command is received via a private channel, just pass it along to the plugin directly
            if(privateChannels.containsKey(channel)){
                privateChannels.get(channel).privateMessagePosted(message, channel, username, userid);

            //if it is received on a normal channel, send the help via a DM to the user
            }else {
                String output = "*russbot knows these commands on #" + channel + "*:\n";
                output += "\t!help | !commands | !about - print this help information\n";

                //get help information from loaded plugins
                String pluginInfo = "";
                for (PluginContainer pc : session.plugins) {

                    //only show help for plugins that are valid on that channel
                    if (pc.channels.contains(channel)) {
                        pluginInfo += "\t*" + pc.plugin.getClass().getSimpleName() + "*: " + pc.plugin.getInfo() + "\n";
                        for (String cmd : pc.plugin.getCommands()) {
                            pluginInfo += "\t\t" + cmd + "\n";
                        }
                    }
                }

                //if at least one plugin is loaded, add that to the output
                if(pluginInfo.length() > 0) {
                    output += "\n*russbot uses these plugins on #" + channel + "*:\n";
                    output += pluginInfo;
                }

                output += "\nvisit https://github.com/russfeld/russbot for more";

                //let the user know where to find the help
                Session.getInstance().sendMessage("I sent you a DM with some helpful information", channel);

                //DM the user with the available help information
                Session.getInstance().sendDirectMessage(output, username);
            }
        }
    }

    /**
     * Class for testing russbot via the command line - handles all input
     */
    private class InputListener{

        MessagePostedListener listen;
        String channel = "test";
        String user = "terminal";

        /**
         * Registers a MessagePostedListener with the system
         * @param listen
         */
        public InputListener(MessagePostedListener listen){
            this.listen = listen;
        }

        /**
         * Thread that runs to listen for user input on the command line
         */
        public void run(){
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("#" + channel + " > ");
            while(true){
                try {
                    String input = reader.readLine();
                    if(input != null && input.length() > 0) {

                        //switch channels with #
                        if(input.startsWith("#")){
                            channel = input.substring(1);
                            System.out.println("You are on channel " + channel);

                        //switch users with @
                        }else if(input.startsWith("@")){
                            user = input.substring(1);
                            System.out.println("You are now user " + user);

                        //pass message to listener
                        }else {
                            listen.onTestEvent(input, channel, user);
                        }
                        System.out.print("#" + channel + " > ");
                    }
                }catch(Exception ex){
                    Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(4);
                }
            }
        }
    }
}
