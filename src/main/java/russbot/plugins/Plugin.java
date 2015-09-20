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
public interface Plugin {
    public abstract String getRegexPattern();
    public abstract String[] getChannels();
    public abstract void messagePosted(String message, String channel);
}
