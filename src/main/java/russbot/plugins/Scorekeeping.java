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
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import russbot.Session;
import russbot.Storage;

/**
 *
 * @author russfeld
 */
public class Scorekeeping implements Plugin{
    private AbstractMap<String, Integer> data;

    public Scorekeeping(){
        data = Storage.getMap("scorekeeping");
    }

    @Override
    public String getRegexPattern() {
        return "((^@??([\\w]{2,})((\\+\\+)|(--))\\z)|(^!score[s]?\\z))";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public String getInfo(){
        return "for keeping score at home";
    }

    @Override
    public String[] getCommands(){
        String[] commands = {
            "<thing>++ - add 1 point to thing's score",
            "<thing>-- - subtract 1 point from things's score",
            "!score | !scores - report the current scores"};
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {
        if(message.startsWith("!score")){
            if(data.size() == 0){
                Session.getInstance().sendMessage("Right now everyone is tied with 0 points", channel);
            }else if(data.size() > 10){
                LinkedList<StringInt> list = new LinkedList<>();
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    String key = entry.getKey();
                    int value = entry.getValue();
                    list.add(new StringInt(key, value));
                }
                Collections.sort(list);
                Iterator<StringInt> it = list.iterator();
                int i = 0;
                String output = "The top 5 scores are:\n";
                while(it.hasNext() && i < 5){
                    StringInt here = it.next();
                    output += "\t" + here.s + ": " + here.i + "\n";
                    i++;
                }
                it = list.descendingIterator();
                i = 0;
                output += "The bottom 5 scores are:\n";
                while(it.hasNext() && i < 5){
                    StringInt here = it.next();
                    output += "\t" + here.s + ": " + here.i + "\n";
                    i++;
                }
                Session.getInstance().sendMessage(output, channel);
            }else{
                String output = "Let's take a look at the scores:\n";
                LinkedList<StringInt> list = new LinkedList<>();
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    String key = entry.getKey();
                    int value = entry.getValue();
                    list.add(new StringInt(key, value));
                }
                Collections.sort(list);
                Iterator<StringInt> it = list.iterator();
                while(it.hasNext()){
                    StringInt here = it.next();
                    output += "\t" + here.s + ": " + here.i + "\n";
                }
                Session.getInstance().sendMessage(output, channel);
            }
        }else{
            String key = message.substring(0, message.length() - 2);
            if(!data.containsKey(key)){
                data.put(key, 0);
            }
            String change = "";
            String[] loseOnly = {"ku", "printers", "printer"};
            if(loseOnly.contains(key.toLowerCase()) && message.charAt(message.length() - 1) == '+') { //TODO remove .toLowerCase() after case checking is implemented
                Session.getInstance().sendMessage(key + " may only lose points.", channel);
            }else{
                if(message.charAt(message.length() - 1) == '+'){
                    data.put(key, data.get(key) + 1);
                    change = "gained";
                }else{
                    data.put(key, data.get(key) - 1);
                    change = "lost";
                }
                Session.getInstance().sendMessage(key + " has " + change + " a point for a total of " + data.get(key) + " points", channel);
            }
        }
    }

    private class StringInt implements Comparable{
        String s;
        int i;

        public StringInt(String ss, int ii){
            s = ss;
            i = ii;
        }

        @Override
        public int compareTo(Object input){
            if(input instanceof StringInt){
                StringInt in = (StringInt)input;
                return in.i - i;
            }else{
                throw new ClassCastException();
            }
        }
    }

}
