/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import russbot.Session;

/**
 *
 * @author russfeld
 */
public class Scorekeeping implements Plugin {
    private HashMap<String, Integer> data;
    private ObjectOutputStream oos;
    
    public Scorekeeping(){
        File file = new File(System.getProperty("user.dir") + File.separator + "data" + File.separator + "scorekeeping.dat");
        if(file.exists()){
            Logger.getLogger(Scorekeeping.class.getName()).log(Level.INFO, "Data file found!");
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                data = (HashMap<String, Integer>)ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Scorekeeping.class.getName()).log(Level.SEVERE, null, ex);
                data = new HashMap();
            } finally {
                try {
                    ois.close();
                } catch (IOException ex) {
                    Logger.getLogger(Scorekeeping.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else{
            Logger.getLogger(Scorekeeping.class.getName()).log(Level.INFO, "Data file not found, creating new file");
            File folder = new File(System.getProperty("user.dir") + File.separator + "data");
            if(!folder.exists()){
                folder.mkdir();
            }
            data = new HashMap();
        }
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
        } catch (IOException ex) {
            Logger.getLogger(Scorekeeping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getRegexPattern() {
        return "^([\\w]+)((\\+\\+)|(--))\\z";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public void messagePosted(String message, String channel) {
        String key = message.substring(0, message.length() - 2);
        if(!data.containsKey(key)){
            data.put(key, 0);
        }
        String change = "";
        if(message.charAt(message.length() - 1) == '+'){
            data.put(key, data.get(key) + 1);
            change = "gained";
        }else{
            data.put(key, data.get(key) - 1);
            change = "lost";
        }
        Session.getInstance().sendMessage(key + " has " + change + " a point for a total of " + data.get(key) + " points", channel);
        try {
            oos.writeObject(data);
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(Scorekeeping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
