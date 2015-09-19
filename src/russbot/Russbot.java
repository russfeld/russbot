/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import russbot.plugins.Plugin;

/**
 *
 * @author russfeld
 */
public class Russbot {
    
    public Russbot(){
        loadPlugins();
        connect();
    }
    
    public void loadPlugins(){
        //http://stackoverflow.com/questions/12730463/how-do-i-call-a-constructor-from-a-class-loaded-at-runtime-java
        File file  = new File(System.getProperty("user.dir") + File.separator + "build" + File.separator + "classes" + File.separator);
        URL url = null;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(4);
        }  
        URL[] urls = new URL[]{url};
        ClassLoader cl = new URLClassLoader(urls);
        for(int i = 0; i < urls.length; i++){
            String className = "FortyTwoPlugin";
            try {
                Class clas = cl.loadClass("russbot.plugins." + className);
                Constructor con = clas.getConstructor();
                Plugin plugin = (Plugin) con.newInstance();
                Session.getInstance().addPlugin(plugin);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(5);
            } catch (SecurityException ex) {
                Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(6);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(7);
            } catch (InstantiationException ex) {
                Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(8);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(9);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(10);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Russbot.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(11);
            }
        }
    }
    
    
    public void connect(){
        Session.getInstance().connect();
    }
    
    public static void main(String[] args){
        new Russbot();
    }

    /**
     * @param args the command line arguments
     */
    public static void main_old(String[] args) throws Exception{
            
    final SlackSession session = SlackSessionFactory.
      createWebSocketSlackSession("xoxb-10601098338-IGe2ygA8ptboghDoAL0KEMmu");
    
    session.addMessagePostedListener(new SlackMessagePostedListener()
    {
        @Override
        public void onEvent(SlackMessagePosted event, SlackSession session)
        {
            
        }
    });
    


    while (true)
    {
      Thread.sleep(1000);
    }
   }
    
}
