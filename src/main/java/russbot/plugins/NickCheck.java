/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import java.util.Random;
import russbot.Session;
/**
 *
 * @author JakeEhrlich
 */
public class NickCheck implements Plugin {
    
    public NickCheck() {}

    @Override
    public String getRegexPattern() {
        return "nick";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }
    
    @Override
    public String getInfo(){
        return "for curing nick of his addiction";
    }
    
    @Override
    public String[] getCommands(){
        String[] commands = {"! - just checks if nick has made a dad joke today"};
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {
        Session.getInstance().sendMessage("Yes");
    }
}
