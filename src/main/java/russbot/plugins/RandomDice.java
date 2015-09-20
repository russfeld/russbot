/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import java.util.Random;
import russbot.Session;
/**
 *
 * @author russfeld
 */
public class RandomDice implements Plugin {
    
    Random random;
    
    public RandomDice(){
        random = new Random();
    }

    @Override
    public String getRegexPattern() {
        return "^![\\d]+d[\\d]+\\z";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public void messagePosted(String message, String channel) {
        String[] split = message.substring(1).split("d");
        int numDice = 0;
        int numSides = 0;
        try{
            numDice = Integer.parseInt(split[0]);
            numSides = Integer.parseInt(split[1]);
        }catch(NumberFormatException e){
            Session.getInstance().sendMessage("That's a very strange number! I can't seem to understand it", channel);
        }
        if(numDice > 20 || numDice <= 0){
            Session.getInstance().sendMessage("I don't have that many dice!", channel);
        }else if(numSides > 100000000 || numSides <= 0){
            Session.getInstance().sendMessage("Are you trying to cause an integer overflow?", channel);
        }else{
            String output = "You rolled ";
            if(numDice == 1){
                int next = random.nextInt(numSides) + 1;
                output += "a " + next;
                if(next == numSides){
                    output += ". You scored a critical hit!";
                }
            }else if (numDice == 2){
                int sum = 0;
                int next = random.nextInt(numSides) + 1;
                sum += next;
                output += next;
                next = random.nextInt(numSides) + 1;
                sum += next;
                output += " and " + next + " for a total of " + sum;
            }else{
                int sum = 0;
                while(numDice > 1){
                    int next = random.nextInt(numSides) + 1;
                    sum += next;
                    output += next + ", ";
                    numDice--;
                }
                int next = random.nextInt(numSides) + 1;
                sum += next;
                output += "and " + next + " for a total of " + sum;
            }
            Session.getInstance().sendMessage(output, channel);
        } 
    }
}
