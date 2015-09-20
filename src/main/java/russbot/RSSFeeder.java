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
import com.rometools.fetcher.impl.HashMapFeedInfoCache;
import com.rometools.fetcher.impl.HttpURLFeedFetcher;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author russfeld
 */
public class RSSFeeder implements Runnable {
    private URL feedURL;
    private FeedFetcher fetcher;
    
    public RSSFeeder(String url){
        feedURL = null;
        try {
            feedURL = new URL(url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(RSSFeeder.class.getName()).log(Level.SEVERE, null, ex);
        }
        final FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
        fetcher = new HttpURLFeedFetcher(feedInfoCache);
        final FetcherEventListenerImpl listener = new FetcherEventListenerImpl();
        fetcher.addFetcherEventListener(listener);
    }
                

    @Override
    public void run() {
        try {
            fetcher.retrieveFeed(feedURL);
            Thread.sleep(300000);
        } catch (IllegalArgumentException | IOException | FeedException | FetcherException | InterruptedException ex) {
            Logger.getLogger(RSSFeeder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class FetcherEventListenerImpl implements FetcherListener {
        /**
         * @see com.rometools.rome.fetcher.FetcherListener#fetcherEvent(com.rometools.rome.fetcher.FetcherEvent)
         */
        @Override
        public void fetcherEvent(final FetcherEvent event) {
            final String eventType = event.getEventType();
            if (FetcherEvent.EVENT_TYPE_FEED_POLLED.equals(eventType)) {
                Logger.getLogger(RSSFeeder.class.getName()).log(Level.INFO, "Feed Polled. URL = " + event.getUrlString());
            } else if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(eventType)) {
                Logger.getLogger(RSSFeeder.class.getName()).log(Level.INFO, "Feed Retrieved. URL = " + event.getUrlString());
            } else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(eventType)) {
                Logger.getLogger(RSSFeeder.class.getName()).log(Level.INFO, "Feed Unchanged. URL = " + event.getUrlString());
            }
        }
    }
}
