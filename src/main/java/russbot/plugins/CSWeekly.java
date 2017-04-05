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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;


/**
 *
 * @author keisenb
 */
public class CSWeekly implements Plugin {

    private final String WEBSITE_URL = "https://testing.atodd.io/newsletter-generator/public/";


    @Override
    public String getRegexPattern() {
        return "![Nn]ews .*|![Nn]ews|![Nn]ews help";
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
            "!news help - Returns a specific event and it's details.",
            "!news <day of week> - Returns the news for a specific day of the upcoming week."
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {

        if (message.toLowerCase().startsWith("!news help")) {


            String msg = "This is the help message";
            Session.getInstance().sendMessage(msg, channel);

        } else if (message.toLowerCase().startsWith("!news ")) {

            String msg = message.substring(6);
            //todo
            Session.getInstance().sendMessage(msg, channel);

        } else if(message.toLowerCase().startsWith("!news")) {

            String msg = allNewsRequest(WEBSITE_URL);
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
            String monday = "";
            String tuesday = "";
            String wednesday = "";
            String thursday = "";
            String friday = "";




            for(int x = 0; x < articles.length(); x ++) {

                JSONObject article = articles.getJSONObject(x);
                String date = "test", location;
                String title = article.getString("title");

                if(article.get("date").toString() == "null") {
                    date = "N/A";
                } else {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try
                    {
                        Date d = simpleDateFormat.parse(article.getString(("date")));
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(d);
                        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                            date = "Monday";
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
                            date = "Tuesday";
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
                            date = "Wednesday";
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
                            date = "Thursday";
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                            date = "Friday";
                        }

                        //date = d.toString();
                    }
                    catch (Exception ex)
                    {
                        System.out.println("Exception "+ex.toString());
                    }
                }
                if(article.get("location").toString() == "null") {
                    location = "N/A";
                } else {
                    location = article.getString("location");
                }
                message +=  date + "\n" + "\t\u2022 " + title + " - " + location + "\n";
            }

            return message;


        } catch (Exception ex) {
            return ex.toString();
        }
    }
}
