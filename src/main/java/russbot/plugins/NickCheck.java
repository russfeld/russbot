/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import russbot.Session;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 *
 * @author JakeEhrlich
 */
public class NickCheck implements Plugin {

    static volatile boolean hasHitUpdate = false;

    SitePoller poller;

    public NickCheck() {
        Session session = Session.getInstance();
        poller = new SitePoller("http://hasnicktoldhisdadjoketoday.com/api/isyes");
        poller.start(); //start the thread so that we can start polling
    }

    //from here: http://stackoverflow.com/questions/1359689/how-to-send-http-request-in-java
    public static int excuteGet(String targetURL) {
        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int ret = connection.getResponseCode();
            System.out.println(ret);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
    static final int noCode = 404;
    static final int yesCode = 200;

    class SitePoller extends Thread {

        String url;

        SitePoller(String url) {
            this.url = url;
        }

        public void unsafeWait(long milli) {
            try {
                Thread.sleep(milli); //wait for 30 minutes
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        public void waitUntilMidnight() {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long howMany = (c.getTimeInMillis()-System.currentTimeMillis());
            this.unsafeWait(howMany);
        }

        public void run() {
            unsafeWait(1000 * 60); //we need to wait for everything to start up
            while(true) {
                int status = excuteGet(url);
                System.out.println("polling the site");
                System.out.println(status);
                if(status == yesCode) {
                    //inform the plugin to not hit the server
                    hasHitUpdate = true;
                    //write out that nick told his joke
                    System.out.println("Nick told his joke for the day!");
                    Session.getInstance().sendMessage("@nickboen has told his dad joke for the day: http://hasnicktoldhisdadjoketoday.com/", "random");
                    //sleep until about next reset time
                    waitUntilMidnight();
                    hasHitUpdate = false;
                    unsafeWait(1000 * 60 * 5); //wait 5 minutes to give Chris's server some time
                } else if(status == noCode) {
                    unsafeWait(1000 * 60 * 15);
                } else {
                    //Session.getInstance().sendMessage("NickCheck messed up", "test");
                }
            }
        }
    }

    @Override
    public String getRegexPattern() {
        return "!(dadpls|nickpls)"; //did someone report nick?
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public String getInfo() {
        return "!nickpls or !dadpls - informs the world that nick has made a dad joke for the day";
    }

    @Override
    public String[] getCommands(){
        String[] commands = {};
        return commands;
    }

    //http://hasnicktoldhisdadjoketoday.com/updateno/jakeihatethatyoumademeaddthis
    //don't do anything right now
    @Override
    public void messagePosted(String message, String channel) {
        excuteGet("http://hasnicktoldhisdadjoketoday.com/updateYes");
    }
}
