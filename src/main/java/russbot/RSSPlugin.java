/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

/**
 *
 * @author russfeld
 */
public interface RSSPlugin {
    public void feedUpdated(String key);
    public void newEntry(SyndEntry entry, String key);
}
