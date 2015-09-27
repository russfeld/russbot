/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot;

import java.net.URL;

import com.rometools.fetcher.FeedFetcher;
import com.rometools.fetcher.FetcherEvent;
import com.rometools.fetcher.FetcherException;
import com.rometools.fetcher.FetcherListener;
import com.rometools.fetcher.impl.FeedFetcherCache;
import com.rometools.fetcher.impl.HttpURLFeedFetcher;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.io.FeedException;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author russfeld
 */
public class RSSFeed implements Runnable{
    private URL feedURL;
    private FeedFetcher fetcher;
    private LinkedList<SyndEntry> entries;
    private SyndFeed feed;
    private String key;
    
    public RSSFeed(String url, RSSPlugin plugin, String aKey){
        feedURL = null;
        try {
            feedURL = new URL(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(RSSFeed.class.getName()).log(Level.SEVERE, null, ex);
        }
        final FeedFetcherCache feedInfoCache = PersistentFeedFetcherCache.getInstance();
        fetcher = new HttpURLFeedFetcher(feedInfoCache);
        
        entries = new LinkedList<>();
        
        try {
            feed = fetcher.retrieveFeed(feedURL);
        } catch (IllegalArgumentException | IOException | FeedException | FetcherException ex) {
            Logger.getLogger(RSSFeed.class.getName()).log(Level.SEVERE, null, ex);
        }
        entries.addAll(feed.getEntries());
        
        final FetcherEventListenerImpl listener = new FetcherEventListenerImpl(plugin);
        fetcher.addFetcherEventListener(listener);
        key = aKey;
        new Thread(this).start();
    }
    
    public LinkedList<SyndEntry> getEntries(){
        return entries;
    }
    
    @Override
    public void run() {
        while(true){
            try {
                fetcher.retrieveFeed(feedURL);
                Thread.sleep(300000);
            } catch (IllegalArgumentException | IOException | FeedException | FetcherException | InterruptedException ex) {
                Logger.getLogger(RSSFeed.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static SlackAttachment formatEntry(SyndEntry entry){
        SlackAttachment slacka = new SlackAttachment();
        String text = "";
        if(entry.getTitle() != null){
            slacka.setTitle(entry.getTitle().trim());
            slacka.setFallback(entry.getTitle().trim());
        }else{
            slacka.setTitle("RSS Entry");
            slacka.setFallback("RSS Entry");
        }
        if(entry.getDescription() != null){
            text += entry.getDescription().getValue() + "\n";
            slacka.setFallback(slacka.fallback + " - " + entry.getDescription().getValue());
        }
        if(entry.getLink() != null){
            slacka.setTitleLink(entry.getLink());
        }
        if(entry.getPublishedDate() != null){
            slacka.addField("Published", entry.getPublishedDate().toString(), true);
        }
        if(entry.getUpdatedDate() != null){
            slacka.addField("Updated", entry.getUpdatedDate().toString(), true);
        }
        if(entry.getAuthors().size() > 0){
            SyndPerson author = entry.getAuthors().get(0);
            slacka.addMiscField("author_name", author.getName());
            slacka.addMiscField("author_link", author.getUri());
        }
        slacka.setText(text);
        return slacka;
    }

    private class FetcherEventListenerImpl implements FetcherListener {
        private RSSPlugin plugin;
        
        public FetcherEventListenerImpl(RSSPlugin p){
            plugin = p;
        }
        
        private void feedUpdated(){
            for(SyndEntry entry : feed.getEntries()){
                if(!entries.contains(entry)){
                    plugin.newEntry(entry, key);
                }
            }
            entries.clear();
            entries.addAll(feed.getEntries());
            entries.sort(new Comparator<SyndEntry>(){
                @Override
                public int compare(SyndEntry o1, SyndEntry o2) {
                    return o1.getPublishedDate().compareTo(o2.getPublishedDate());
                }
                
            });
        }
        
        /**
         * @see com.rometools.rome.fetcher.FetcherListener#fetcherEvent(com.rometools.rome.fetcher.FetcherEvent)
         */
        @Override
        public void fetcherEvent(final FetcherEvent event) {
            final String eventType = event.getEventType();
            if (FetcherEvent.EVENT_TYPE_FEED_POLLED.equals(eventType)) {
                //Feed is polled but hasn't responded yet
            } else if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(eventType)) {
                Logger.getLogger(RSSFeed.class.getName()).log(Level.INFO, "Feed Retrieved. URL = " + event.getUrlString());
                feed = event.getFeed();
                feedUpdated();
                plugin.feedUpdated(key);
            } else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(eventType)) {
                //Feed has not changed since last poll
            }
        }
    }
}
