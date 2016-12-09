/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import java.net.URLEncoder;
import russbot.Session;

/**
 *
 * @author at0dd
 */
public class LMGTFY implements Plugin {

    @Override
    public String getRegexPattern() {
        return "!LMGTFY .*|!lmgtfy .*|!lmgtfye .*|!LMGTFYE .*";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public String getInfo(){
        return "Let Me Google that for You";
    }

    @Override
    public String[] getCommands(){
        String[] commands = {
            "!lmgtfy [string] - Get a link to Google something.",
            "!lmgtfye [string] - Get a link to Google something and explain how to use the internet."
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {
      String url = "Error: failed to get URL.";
      if(message.toLowerCase().startsWith("!lmgtfye")){
        String key = message.substring(9);
        String encoded = "";
        try {
          encoded = URLEncoder.encode(key, "UTF-8");
        } catch (Exception ex){
          System.out.println("Error, could not encode URL.\n" + ex);
        }
        url = "http://lmgtfy.com/?iie=1&q=" + encoded;
      } else if(message.toLowerCase().startsWith("!lmgtfy")){
        String key = message.substring(8);
        String encoded = "";
        try {
          encoded = URLEncoder.encode(key, "UTF-8");
        } catch (Exception ex){
          System.out.println("Error, could not encode URL.\n" + ex);
        }
        url = "http://lmgtfy.com/?q=" + encoded;
      }
        Session.getInstance().sendMessage(url, channel);
    }

}
