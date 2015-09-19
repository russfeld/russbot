/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot;

import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import russbot.plugins.Plugin;

/**
 *
 * @author russfeld
 */
public final class Session {
    private static Session session;
    private SlackSession slacksession;
    private List<PluginContainer> plugins;
    
    private Session(){
        
    }
    
    public static Session getInstance(){
        if(session == null){
            session = new Session();
            session.plugins = new LinkedList<>();
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
    
    public void connect(){
        try {
            session.slacksession.addMessagePostedListener(new MessagePostedListener());
            session.slacksession.connect();
        } catch (IOException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(3);
        }
    }
    
    public void addPlugin(Plugin p){
        plugins.add(new PluginContainer(p));
    }
    
    public void sendMessage(String message, String channel){
        session.slacksession.sendMessage(session.slacksession.findChannelByName(channel), message, null);
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
            String message = event.getMessageContent();
            for(PluginContainer pc : session.plugins){
                if(pc.channels.contains(channel)){
                    if(pc.pattern.matcher(message).matches()){
                        pc.plugin.messagePosted(message, channel);
                    }
                }
                
            }
   
                /*
                //let's send a message
                SlackMessageHandle handle = session.sendMessage(event.getChannel(),
                                    event.getMessageContent(), null);
                try
                {
                    Thread.sleep(2000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                //2 secs later, let's update the message (I can only update my own messages)
                session.updateMessage(handle.getSlackReply().getTimestamp(),event.getChannel(),
                                      event.getMessageContent()+" UPDATED");
                try
                {
                    Thread.sleep(2000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                //2 secs later, let's now delete the message (I can only delete my own messages)
                session.deleteMessage(handle.getSlackReply().getTimestamp(),event.getChannel());
                */
        }
        
    }
}
