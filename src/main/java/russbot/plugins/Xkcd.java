/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.net.URL;

import russbot.Session;

/**
 *
 * @author V-FEXrt (Ashley Coleman)
 */
public class Xkcd implements Plugin {
    private Random rand = new Random();

    @Override
    public String getRegexPattern() {
        return "!xkcd.*";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public String getInfo(){
        return "Posts a xkcd comic";
    }

    @Override
    public String[] getCommands(){
        String[] commands = {
            "!xkcd - get a random xkcd comic",
            "!xkcd new - get the most recent xkcd comic",
            "!xkcd <number> - get xkcd with id <number>"
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {
        String response = "";

        if(message.length() > 5){
            String param = message.substring(6);
            if("new".equals(param)){
                response = newestComic();
            }else{
                response = comicByID(param);
            }
        }else{
            response = randomComic();
        }

        Session.getInstance().sendMessage(response, channel);
    }

    private int getMaxComicID(){
        String resp = executeGet("http://xkcd.com/");
        String permaLink = executeRegex("Permanent link to this comic: http:\\/\\/xkcd.com\\/\\d+\\/", resp);
        String maxID = executeRegex("\\d+", permaLink);

        try {
            return Integer.parseInt(maxID);
        } catch(NumberFormatException e){
            return 1776; // This is the current max - 12-23/16 -AC
        }
    }

    private String newestComic(){
        return buildURL(getMaxComicID());
    }

    private String randomComic(){
        return buildURL(rand.nextInt(getMaxComicID()) + 1);
    }

    private String comicByID(String input){
        try {
            int id = Integer.parseInt(input);
            if(id <= getMaxComicID()){
                return buildURL(id);
            }
            return "Cannot find comic with id: " + Integer.toString(id);
        } catch(NumberFormatException e){
            return "Cannot process id: " + input;
        }
    }

    private String buildURL(int id){
        return "http://xkcd.com/" + Integer.toString(id);
    }

    //from here: http://stackoverflow.com/questions/1359689/how-to-send-http-request-in-java
    private String executeGet(String targetURL) {
        String response = "";

        try {
            URL url = new URL(targetURL);
            InputStream is = url.openStream();
            response = convertStreamToString(is);
            is.close();
        } catch(Exception e) {
        }

        return response;
    }

    // from here: http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    // from here: http://stackoverflow.com/questions/237061/using-regular-expressions-to-extract-a-value-in-java
    private String executeRegex(String regex, String text) {
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(text);
      m.find();
      return m.group();
    }
}
