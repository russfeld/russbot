/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot;

import com.rometools.fetcher.impl.FeedFetcherCache;
import com.rometools.fetcher.impl.SyndFeedInfo;
import java.net.URL;
import java.util.AbstractMap;

/**
 *
 * @author russfeld
 */
public class PersistentFeedFetcherCache implements FeedFetcherCache {
    
    private static PersistentFeedFetcherCache cache;
    private AbstractMap<String, SyndFeedInfo> data;
    
    private PersistentFeedFetcherCache(){
        data = Storage.getMap("feeds");
    }
    
    public static PersistentFeedFetcherCache getInstance(){
        if(cache == null){
            cache = new PersistentFeedFetcherCache();
        }
        return cache;
    }

    @Override
    public SyndFeedInfo getFeedInfo(URL url) {
        return data.get(url.toString());
    }

    @Override
    public void setFeedInfo(URL url, SyndFeedInfo sfi) {
        data.put(url.toString(), sfi);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public SyndFeedInfo remove(URL url) {
        return data.remove(url.toString());
    }
    
}
