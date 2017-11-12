/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import java.util.regex.Pattern;

/**
 *
 * @author russfeld
 */
public abstract class Plugin {
    public abstract String getRegexPattern();
    public abstract String[] getChannels();
    public abstract String getInfo();
    public abstract String[] getCommands();
    public void messagePosted(String message, String channel){}
    public void messagePosted(String message, String channel, String username, String userid){
        messagePosted(message, channel);
    }
    public void privateMessagePosted(String message, String channel, String username, String userid){}
}
