/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import russbot.Session;
import com.mashape.unirest.http.JsonNode;
import org.json.JSONObject;

/**
 *
 * @author keisenb
 */
public class BeocatBreakIn implements Plugin {

    BeocatGame game = null;

    private final String BEGIN_URL = "https://testing.atodd.io/api/begin";
    private final String PLAY_URL  = "https://testing.atodd.io/api/play";
    private final String END_URL   = "https://testing.atodd.io/api/end";


    @Override
    public String getRegexPattern() {
        return "![Bb]eocat start .*|![Bb]eocat go .*|![Bb]eocat look .*|![Bb]eocat examine .*|![Bb]eocat take .*|![Bb]eocat drop .*|![Bb]eocat kill!|![Bb]eocat wait|![Bb]eocat wear .*|![Bb]eocat use .*|![Bb]eocat help|![Bb]eocat inventory|![Bb]eocat save|![Bb]eocat load|![Bb]eocat reset|![Bb]eocat end";
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
            "!beocat end - Quits the game when you are finished playing."
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {

        String command     = message.toLowerCase().substring(8);
        JSONObject headers = new JSONObject();


        if(message.toLowerCase().startsWith("!beocat start")){

            String gameName = command.substring(6);

            try {
              HttpResponse<JsonNode> response = Unirest.post(BEGIN_URL)
              .header("game-name", gameName)
              .asJson();
              JsonNode body = response.getBody();
              String msg = body.getObject().getString("user-response");
              int game_id = body.getObject().getInt("game-id");

              game = new BeocatGame(command, game_id);
              Session.getInstance().sendMessage(msg, channel);

            } catch (Exception ex){
               Session.getInstance().sendMessage("Opps! There was an error with the Beocat Break-In API.", channel);
            }

          }
          else if(message.startsWith("!beocat go")) {

            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);


            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat look")) {


            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat examine")) {


            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat take")) {


            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat drop")) {


            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat kill")) { //test from here


            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat wait")) {

            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat wear")) {


            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat use")) {


            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat help")) {

            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat inventory")) {

            //String command = "";
            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat save")) {


            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat load")) {

            //String command = "";
            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat reset")) {

            String name = game.getName();
            int game_id = game.getId();
            //String command = "";
            headers.put("game-name", game.getName());
            headers.put("user-request", command);
            String result = PostRequest(PLAY_URL, headers);

            game = new BeocatGame(name, game_id);

            Session.getInstance().sendMessage(result, channel);
          }
          else if(message.toLowerCase().startsWith("!beocat end")) {

            try {
              HttpResponse<JsonNode> response = Unirest.post(END_URL)
              .header("game-id", Integer.toString(game.getId()))
              .asJson();
              JsonNode body = response.getBody();
              String msg = body.getObject().getString("user-response");
              Session.getInstance().sendMessage(msg, channel);
              game = null;

            } catch (Exception ex){
               Session.getInstance().sendMessage("Opps! There was an error with the Beocat Break-In API.", channel);
            }

          }
    }


    public String PostRequest(String url, JSONObject headers) {
      try {
        HttpResponse<JsonNode> response = Unirest.post(url)
        .header("game-name", headers.getString("game-name"))
        .header("user-request", headers.getString("user-request"))
        .asJson();
        JsonNode body = response.getBody();
        String msg = body.getObject().getString("user-response");
        return msg;

      } catch (Exception ex){

        return "Opps! There was an error with the Beocat Break-In API.";
      }
    }



    private class BeocatGame {

      private String _name;
      private int _id;

      public BeocatGame(String name, int id) {
        _name = name;
        _id = id;
      }

      public String getName() {
       return _name;
      }

      public int getId() {
        return _id;
      }


    }




}
