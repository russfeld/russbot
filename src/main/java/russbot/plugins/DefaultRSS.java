/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import com.rometools.rome.feed.synd.SyndEntry;
import java.util.HashMap;
import java.util.Iterator;
import russbot.RSSFeed;
import russbot.RSSPlugin;
import russbot.Session;

/**
 *
 * @author russfeld
 */
public class DefaultRSS extends Plugin implements RSSPlugin{
    HashMap<String, RSSFeed> feeds;
    String[] channels = {"test"};
    
    public DefaultRSS(){
        feeds = new HashMap<>();
        feeds.put("russbot", new RSSFeed("https://github.com/russfeld/russbot/commits/master.atom", this, "russbot"));
        feeds.put("acm", new RSSFeed("https://orgsync.com/calendar/org/rss/3432595/44766b586c577c776c208b95b16cf32ff6e00484?org=86744", this, "acm"));
    }

    @Override
    public String getRegexPattern() {
        return "^!rss [\\w]+\\z";
    }

    @Override
    public String[] getChannels() {
        return channels;
    }

    @Override
    public String getInfo() {
        return "reads and posts from RSS feeds";
    }

    @Override
    public String[] getCommands() {
        String feedList = "";
        for(String key : feeds.keySet()){
            feedList += key + ", ";
        }
        feedList = feedList.substring(0, feedList.length() - 2);
        String[] commands = {"!rss <feed> - get last 5 posts from feed", "\tFeeds: " + feedList};
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {
        String feedName = message.replace("!rss ", "");
        if(feeds.containsKey(feedName)){
            Session.getInstance().sendMessage("Most recent entries from " + feedName, channel);
            
            RSSFeed feed = feeds.get(feedName);
            Iterator<SyndEntry> iter = feed.getEntries().iterator();
            int i = 0;
            while(i < 5 && iter.hasNext()){
                SyndEntry entry = iter.next();
                Session.getInstance().sendMessage(null, channel, RSSFeed.formatEntry(entry));
                i++;
            }
        }else{
            Session.getInstance().sendMessage("I don't recognize that feed name", channel);
        }
    }

    @Override
    public void feedUpdated(String key) {
        //if you want to manage your feed entries yourself, you can do so here!
    }

    @Override
    public void newEntry(SyndEntry entry, String key) {
        String output = "RSS: New entry from " + key + ":\n";
        for(String channel : channels){
            Session.getInstance().sendMessage(output, channel, RSSFeed.formatEntry(entry));
        }
    }
    
}
