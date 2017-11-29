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
 *
 * @author russfeld
 */
public final class Session {
    private static Session session;
    private SlackSession slacksession;
    private List<PluginContainer> plugins;
    private HashMap<String, Plugin> privateChannels;

    private static boolean test = false;

    private Session(){

    }

    public static Session getInstance(){
        if(session == null){
            session = new Session();
            session.plugins = new LinkedList<>();
            session.privateChannels = new HashMap<>();
        }
        if(session.slacksession == null){
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
            session.slacksession = SlackSessionFactory.createWebSocketSlackSession(properties.getProperty("token"));
        }
        return session;
    }

    public void connect(boolean test){
        if(test){
            this.test = true;
            InputListener input = new InputListener(new MessagePostedListener());
            System.out.println("TESTING MODE!");
            System.out.println("To change channel, type # followed by a channel name (like #random)");
            System.out.println("To change user, type @ followed by a user name (like @user)");
            input.run();
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

    public void addPlugin(Plugin p){
        plugins.add(new PluginContainer(p));
        Logger.getLogger(Session.class.getName()).log(Level.INFO, "Plugin " + p.getClass().getCanonicalName() + " registered");
    }

    public String registerPrivateChannel(String[] usernames, Plugin plugin){
        if(test){
            sendMessage("```Created private channel for users " + Arrays.toString(usernames) + " to plugin " + plugin.getClass().getName() + "```", "terminal");
            sendMessage("```Use #terminal-" + Arrays.toString(usernames) + " to connect```", "terminal");
            privateChannels.put("terminal-" + Arrays.toString(usernames), plugin);
            return "terminal-" + Arrays.toString(usernames);
        }else {
            String channel = null;
            if (usernames.length > 1) {
                LinkedList<SlackUser> users = new LinkedList<SlackUser>();
                for (String username : usernames) {
                    users.add(session.slacksession.findUserByUserName(username));
                }
                SlackMessageHandle<SlackChannelReply> reply = session.slacksession.openMultipartyDirectMessageChannel(users.toArray(new SlackUser[]{}));
                channel = reply.getReply().getSlackChannel().getId();
            } else {
                SlackUser user = session.slacksession.findUserByUserName(usernames[0]);
                SlackMessageHandle<SlackChannelReply> reply = session.slacksession.openDirectMessageChannel(user);
                channel = reply.getReply().getSlackChannel().getId();
            }
            if (channel == null) {
                Logger.getLogger(Session.class.getName()).log(Level.INFO, "Unable to register private channel for " + plugin.getClass().getName());
                return null;
            }
            if (privateChannels.containsKey(channel)) {
                session.sendPrivateMessage("```Disconnecting from " + privateChannels.get(channel).getClass().getName() + "...```", channel);
            }
            privateChannels.put(channel, plugin);
            session.sendPrivateMessage("```You are now connected to " + plugin.getClass().getName() + "...```", channel);
            return channel;
        }
    }

    public void unregisterPrivateChannel(String channel){
        if(privateChannels.containsKey(channel)){
            session.sendPrivateMessage("```Disconnecting from " + privateChannels.get(channel).getClass().getName() + "...```", channel);
            privateChannels.remove(channel);
        }
    }

    public void sendMessage(String message, String channel){
        sendMessage(message, channel, null);
    }

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

    public void sendDirectMessage(String message, String user){
        sendDirectMessage(message, user, null);
    }

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

    public void sendPrivateMessage(String message, String channel){
        sendPrivateMessage(message, channel, null);
    }

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

    private class PluginContainer{
        public Pattern pattern;
        public HashSet channels;
        public Plugin plugin;

        public PluginContainer(Plugin p){
            pattern = Pattern.compile(p.getRegexPattern());
            channels = new HashSet();
            for(String s : p.getChannels()){
                channels.add(s);
            }
            plugin = p;
        }
    }

    private class MessagePostedListener implements SlackMessagePostedListener{

        @Override
        public void onEvent(SlackMessagePosted event, SlackSession ss) {
            String channel = event.getChannel().getName();
            String message = StringEscapeUtils.unescapeHtml4(event.getMessageContent());
            String username = event.getSender().getUserName();
            String userid = event.getSender().getId();
            handleMessage(channel, message, username, userid);
        }

        public void onTestEvent(String input, String channel, String user){
            handleMessage(channel, input, user, "U1234ABCD");
        }

        private void handleMessage(String channel, String message, String username, String userid){
            if(message.equals("!help") || message.equals("!commands") || message.equals("!about")){
                String output = "*russbot knows these basic commands*:\n";
                output +="\t!help | !commands | !about - print this help information\n";
                //more commands here
                output +="\n*russbot uses these plugins*:\n";
                for(PluginContainer pc : session.plugins){
                    output +="\t*" + pc.plugin.getClass().getSimpleName() + "*: " + pc.plugin.getInfo() + "\n";
                    for(String cmd : pc.plugin.getCommands()){
                        output +="\t\t" + cmd + "\n";
                    }
                }
                output +="\nvisit https://github.com/russfeld/russbot for more";
                Session.getInstance().sendMessage(output, channel);
            }else{
                //debugging only
                //System.out.println(channel + ":" + message + ":" + username + ":" + userid);
                for(PluginContainer pc : session.plugins){
                    if(pc.channels.contains(channel)){
                        if(pc.pattern.matcher(message).matches()){
                            pc.plugin.messagePosted(message, channel, username, userid);
                        }
                    }
                }
                if(privateChannels.containsKey(channel) && !username.equals("russbot")){
                    privateChannels.get(channel).privateMessagePosted(message, channel, username, userid);
                }
            }
        }
    }

    private class InputListener{

        MessagePostedListener listen;
        String channel = "test";
        String user = "terminal";

        public InputListener(MessagePostedListener listen){
            this.listen = listen;
        }

        public void run(){
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("#" + channel + " > ");
            while(true){
                try {
                    String input = reader.readLine();
                    if(input != null && input.length() > 0) {
                        if(input.startsWith("#")){
                            channel = input.substring(1);
                            System.out.println("You are on channel " + channel);
                        }else if(input.startsWith("@")){
                            user = input.substring(1);
                            System.out.println("You are now user " + user);
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

    public static String getApiKey(String keyName){
      Properties properties = new Properties();
      try {
          properties.load(new FileInputStream("russbot.cfg"));
      } catch (IOException ex) {
          Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
          System.out.println("Error reading russbot.cfg!");
      }
      return properties.getProperty(keyName).toString();
    }
}
