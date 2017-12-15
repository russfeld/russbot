/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import russbot.Session;

/**
 *
 * @author at0dd
 */
public class Ping extends Plugin {

    @Override
    public String getRegexPattern() {
        return "![Pp]ing";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test", "random"};
        return channels;
    }

    @Override
    public String getInfo(){
        return "Ping Pong";
    }

    @Override
    public String[] getCommands(){
        String[] commands = {
            "!ping - send a ping to russbot"
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {
        Session.getInstance().sendMessage("Pong!", channel);
    }

}
