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
import org.json.JSONArray;


/**
 *
 * @author keisenb
 */
public class CSWeekly implements Plugin {

    private final String WEBSITE_URL = "https://testing.atodd.io/newsletter-generator/public/";


    @Override
    public String getRegexPattern() {
        return "![Nn]ews .*|![Nn]ews";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {
            "test"
        };
        return channels;
    }

    @Override
    public String getInfo() {
        return "CS Weekly Newsletter";
    }

    @Override
    public String[] getCommands() {
        String[] commands = {
            "!news - Returns a list of Computer Science events for the week.",
            "!beocat <event> - Returns a specific event and it's details."
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {

        if (message.toLowerCase().startsWith("!news ")) {

            //specific
            //String msg = BeginRequest(BEGIN_URL, gameName);
            String msg = message.substring(6);
            Session.getInstance().sendMessage(msg, channel);

        } else if(message.toLowerCase().startsWith("!news")) {

            //general
            String msg = "All the news";

            msg = allNewsRequest(WEBSITE_URL);

            Session.getInstance().sendMessage(msg, channel);
        }
    }


    public String formatNews(HttpResponse<String> news) {
        String formatted = "format";


        return formatted;
    }

    public String allNewsRequest(String url) {
        try {
            HttpResponse<String> response = Unirest.get("https://testing.atodd.io/newsletter-generator/public/api/articles").asString();

            String body = response.getBody();
            JSONObject object = new JSONObject(body);
            JSONArray articles = object.getJSONArray("articles");

            String message = "";

            for(int x = 0; x < articles.length(); x ++) {
                JSONObject article = articles.getJSONObject(x);
                String date = article.getString("date");
                String title = article.getString("title");
                String location = article.getString("location");
                String entry = title + " - " + location + " - " + date + "\n";

                message += entry;
            }


            return message;

        } catch (Exception ex) {
            return ex.toString();
        }

    }



}
