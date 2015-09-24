/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot;

import java.util.AbstractMap;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVMap.Builder;
import org.h2.mvstore.MVStore;

/**
 *
 * @author russfeld
 */
public class Storage implements Runnable {
    private static MVStore store;
    
    private static void init(){
        store = MVStore.open("./data/russbot.mvs");
        Runtime.getRuntime().addShutdownHook(new Thread(new Storage()));
    }
    
    public static AbstractMap getMap(String name){
        if(store == null){
            init();
        }
        String callerClassName = new Exception().getStackTrace()[1].getClassName();
        return store.openMap(callerClassName + ":" + name);
    }

    @Override
    public void run() {
        store.close();
    }
}
