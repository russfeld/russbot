/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import russbot.Session;
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
    //Map<int, List<String>> days = new HashMap<int, List<String>>();

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

    /*public void AddArticle(int day, String article) {
        List<String> list = days.get(day);
        if (list == null) {
            list = new List<String>();
            days.put(key, list);
        }
        list.add(article);
    }*/

    public String BuildMessage(JSONArray articles) {
        String message = "", monday = "", tuesday = "", wednesday = "", thursday = "", friday = "", saturday = "", sunday = "", other = "";



        for(int x = 0; x < articles.length(); x ++) {

            JSONObject article = articles.getJSONObject(x);
            String date = "";
            String location = "";
            String link = "";
            String title = "" + article.getString("title");



            if(article.get("location").toString() == "null") {
                location += "N/A";
            } else {
                location = article.getString("location");
            }
            if(!article.isNull("link")) {

                link = article.getString("link");
            }

            if(article.get("date").toString() == "null") {
                if(other == "") {
                    other += "*Other Announcements*\n";
                }
                other += ">\u2022 " + title + "\n";
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try
                {
                    Date d = simpleDateFormat.parse(article.getString(("date")));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);

                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int min =  cal.get(Calendar.MINUTE);
                    String time =  hour%12 + ":" + min + ((min==0) ? "0" : "") + " " + ((hour>=12) ? "PM" : "AM");


                    String web = "";
                    if(link != "") {
                        web = " - <" + link + "| read more>";
                    }

                    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {

                        if(monday == "") {
                            monday += "*Monday*\n";
                        }



                        monday += ">\u2022 " + title+  " @ "  + time + " - " + location + web + "\n";
                    }
                    else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
                        if(tuesday == "") {
                            tuesday += "*Tuesday*\n";
                        }



                        tuesday += ">\u2022 " + title +  " @ " + time + " - " + location +  web + "\n";
                    }
                    else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
                        if(wednesday == "") {
                            wednesday += "*Wednesday*\n";
                        }


                        wednesday += ">\u2022 " + title +  " @ " + time + " - " + location + web + "\n";
                    }
                    else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
                        if(thursday == "") {
                            thursday += "*Thursday*\n";
                        }



                        thursday += ">\u2022 " + title +  " @ " + time + " - " + location + web + "\n";
                    }
                    else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                        if(friday == "") {
                            friday += "*Friday*\n";
                        }


                        friday += ">\u2022 " + title +  " @ " + time + " - " + location + web + "\n";
                    }
                    else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                        if(saturday == "") {
                            saturday += "*Saturday*\n";
                        }
                        saturday += ">\u2022 " + title +  " @ " + time + " - " + location + "\n";
                    }
                    else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        if(sunday == "") {
                            sunday += "*Sunday*\n";
                        }


                        sunday += ">\u2022 " + title +  " @ " + time + " - " + location + web + "\n";
                    }

                }
                catch (Exception ex)
                {
                    System.out.println("Exception "+ex.toString());
                }
            }
        }

        message = monday + tuesday + wednesday + thursday + friday + saturday + sunday + other;
        message += "\n" + "To learn more about these events check out the online newsletter here! " + WEBSITE_URL;

        return message;
    }


    public String allNewsRequest(String url) {
        try {
            HttpResponse<String> response = Unirest.get("https://testing.atodd.io/newsletter-generator/public/api/articles").asString();

            String body = response.getBody();
            JSONObject object = new JSONObject(body);
            JSONArray articles = object.getJSONArray("articles");

            return BuildMessage(articles);

        } catch (Exception ex) {
            return ex.toString();
        }
    }
}
