/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import russbot.Session;

/**
 *
 * @author russfeld
 */
public class FortyTwoPlugin implements Plugin {

    @Override
    public void messagePosted(String message, String channel) {
        Session.getInstance().sendMessage("“The Answer to the Great Question... Of Life, the Universe and Everything... Is... Forty-two,' said Deep Thought, with infinite majesty and calm.” \n" +
"― Douglas Adams, The Hitchhiker's Guide to the Galaxy", channel);
    }

    @Override
    public String getRegexPattern() {
        return "(life[ ,]+the universe[ ,]+and everything)|(\\b42\\b)|(\\b[Ff]orty two\\b)|(\\b[Ff]orty-two\\b)";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }
    
}
