/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import java.net.URL;
import java.net.URLEncoder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import russbot.Session;

/**
 *
 * @author at0dd
 */
public class Wolfram implements Plugin {

    @Override
    public String getRegexPattern() {
        return "!wolfram .*";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public String getInfo(){
        return "Wolfram Alpha Search - maximum 2000 API calls per month.";
    }

    @Override
    public String[] getCommands(){
        String[] commands = {
            "!wolfram <string> - Returns the first result from Wolfram Alpha.",
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {
      if(message.toLowerCase().startsWith("!wolfram ")){
        String appId = Session.getApiKey("wolfram");
        String key = message.substring(9);
        String encoded = "";
        try {
          encoded = URLEncoder.encode(key, "UTF-8");
        } catch (Exception ex){
          System.out.println("Error, could not encode search terms.\n" + ex);
        }
        try {
          DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          Document doc = db.parse(new URL("http://api.wolframalpha.com/v2/query?appid="+appId+"&input="+encoded+"&format=plaintext").openStream());
          String interpretation = doc.getElementsByTagName("pod").item(0).getTextContent();
          String result = doc.getElementsByTagName("pod").item(1).getTextContent();
          Session.getInstance().sendMessage(interpretation.trim() + "\n" + result.trim(), channel);
        } catch (Exception ex){
          Session.getInstance().sendMessage("There was an error with the Wolfram API. Please try a different search.", channel);
        }
      }
    }
}
