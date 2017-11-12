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
public class BeocatBreakIn extends Plugin {

    BeocatGame game = null;

    private final String BEGIN_URL = "https://beocat.keisenb.io/api/begin";
    private final String PLAY_URL  = "https://beocat.keisenb.io/api/play";
    private final String END_URL   = "https://beocat.keisenb.io/api/end";

    @Override
    public String getRegexPattern() {
        return "![Bb]eocat start .*|![Bb]eocat move .*|![Bb]eocat go .*|![Bb]eocat look .*|![Bb]eocat examine .*|![Bb]eocat take .*|![Bb]eocat drop .*|![Bb]eocat kill!|![Bb]eocat wait|![Bb]eocat wear .*|![Bb]eocat use .*|![Bb]eocat help|![Bb]eocat inventory|![Bb]eocat save|![Bb]eocat load|![Bb]eocat reset|![Bb]eocat end";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {
            "test",
            "beocat-break-in"
        };
        return channels;
    }

    @Override
    public String getInfo() {
        return "Beocat Break-In (beta)";
    }

    @Override
    public String[] getCommands() {
        String[] commands = {
            "!beocat start <gameName> - Start a new Beocat Break-In game.",
            "!beocat end - Quits the game when you are finished playing."
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {

        String command = message.toLowerCase().substring(8);

        if (message.toLowerCase().startsWith("!beocat start")) {

            String gameName = command.substring(6);
            String msg      = BeginRequest(BEGIN_URL, gameName);

            Session.getInstance().sendMessage(msg, channel);

        } else if (message.toLowerCase().startsWith("!beocat end")) {

            String msg = EndRequest(END_URL, game.getId());

            Session.getInstance().sendMessage(msg, channel);

        } else {

          JSONObject headers = new JSONObject();
          headers.put("game-id", Integer.toString(game.getId()));
          headers.put("user-request", command);
          String msg = PlayRequest(PLAY_URL, headers);

          Session.getInstance().sendMessage(msg, channel);
        }
    }


    public String PlayRequest(String url, JSONObject headers) {
        try {
            HttpResponse < JsonNode > response = Unirest.post(url)
                .header("game-id", headers.getString("game-id"))
                .header("user-request", headers.getString("user-request"))
                .asJson();
            JsonNode body = response.getBody();
            String msg    = body.getObject().getString("user-response");
            return msg;

        } catch (Exception ex) {
            return "Oops! There was an error with the Beocat Break-In API.";
        }
    }


    public String EndRequest(String url, int gameId) {
      try {
          HttpResponse < JsonNode > response = Unirest.post(END_URL)
              .header("game-id", Integer.toString(gameId))
              .asJson();
          JsonNode body = response.getBody();
          String msg    = body.getObject().getString("user-response");
          game          = null;

          return msg;
        } catch (Exception ex) {
            return "Oops! There was an error with the Beocat Break-In API.";
        }
    }


    public String BeginRequest(String url, String gameName) {
      try {
          HttpResponse < JsonNode > response = Unirest.post(BEGIN_URL)
              .header("game-name", gameName)
              .asJson();
          JsonNode body = response.getBody();
          String intro  = body.getObject().getString("intro");
          String msg    = body.getObject().getString("user-response");
          int game_id   = body.getObject().getInt("game-id");
          game          = new BeocatGame(gameName, game_id);

          return intro + "\n\n" + msg;
        } catch (Exception ex) {
            return "Oops! There was an error with the Beocat Break-In API.";
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
