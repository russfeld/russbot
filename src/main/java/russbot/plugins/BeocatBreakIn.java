/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import russbot.Session;
import java.io.StringReader;
import java.net.URLEncoder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.request.HttpRequestWithBody;

/**
 *
 * @author keisenb
 */
public class BeocatBreakIn implements Plugin {

    BeocatGame game;

    @Override
    public String getRegexPattern() {
        return "![Bb]eocat start .*|![Bb]eocat go .*|![Bb]eocat look .*|![Bb]eocat examine .*|![Bb]eocat take .*|![Bb]eocat drop .*|![Bb]eocat kill!|![Bb]eocat wait|![Bb]eocat wear .*|![Bb]eocat use .*|![Bb]eocat help|![Bb]eocat inventory|![Bb]eocat save|![Bb]eocat load|![Bb]eocat reset|![Bb]eocat quit";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public String getInfo(){
        return "Beocat Break-In";
    }

    @Override
    public String[] getCommands(){
        String[] commands = {
            "!beocat start <gameName> - Start a new Beocat Break-In game.",
            "!beocat help - List the commands for the game."
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {

        String val = message.toLowerCase().substring(8);
        if(message.toLowerCase().startsWith("!beocat start")){
            String test = PostRequest();
            String command = val.substring(6);
            game = new BeocatGame(command);
            Session.getInstance().sendMessage(test, channel);
          }
          else if(message.startsWith("!beocat go")) {

            String command = val.substring(3);
            Session.getInstance().sendMessage("Moved " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat look")) {

            String command = val.substring(5);
            Session.getInstance().sendMessage("Looked at " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat examine")) {

            String command = val.substring(8);
            Session.getInstance().sendMessage("You examine the " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat take")) {

            String command = val.substring(5);
            Session.getInstance().sendMessage("You take the " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat drop")) {

            String command = val.substring(5);
            Session.getInstance().sendMessage("You drop the " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat kill")) { //test from here

            Session.getInstance().sendMessage("You have died.", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat wait")) {

            Session.getInstance().sendMessage("You are waiting around.", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat wear")) {

            String command = val.substring(5);
            Session.getInstance().sendMessage("You put on the " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat use")) {

            String[] items = val.substring(4).split(" ");
            Session.getInstance().sendMessage("You use " + items[0] + " on " + items[1] + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat help")) {

            Session.getInstance().sendMessage(HelpMessage(), channel);
          }
          else if(message.toLowerCase().startsWith("!beocat inventory")) {

            Session.getInstance().sendMessage("Your inventory contains: " + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat save")) {

            int savesLeft = 0;
            Session.getInstance().sendMessage("You have saved the game. You have " + savesLeft + " saves remaining.", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat load")) {

            Session.getInstance().sendMessage("You have loaded your game save.", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat reset")) {

            String name = game.getName();
            game = new BeocatGame(name);
            Session.getInstance().sendMessage("You have reset the game!", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat quit")) {

            game = null;
            Session.getInstance().sendMessage("You have quit the game! Thanks for playing.", channel);
          }
    }

    public String HelpMessage() {
      //api request for commands
      return "";
    }


    public String PostRequest() {
      String key = "game-name:test";
      String encoded = "";
      try {
        encoded = URLEncoder.encode(key, "UTF-8");
      } catch (Exception ex){
        System.out.println("Error, could not encode search terms.\n" + ex);
      }
      try {
        HttpResponse<JsonNode> response = Unirest.post("https://testing.atodd.io/api/begin").header("game-name", "test").asJson();
        JsonNode body = response.getBody();
        //String content = body.get("game-name").textValue();
        String msg = body.getObject().getString("game-name");
        return msg;

      } catch (Exception ex){

        return "There was an error with the Beocat Break-In API.";
      }

    }



    private class BeocatGame {

      private String _name;

      public BeocatGame(String name) {
        _name = name;
      }

      public String getName() {
       return _name;
    }

    }




}
