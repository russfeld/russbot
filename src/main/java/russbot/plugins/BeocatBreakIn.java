/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package russbot.plugins;

import russbot.Session;

/**
 *
 * @author at0dd
 */
public class BeocatBreakIn implements Plugin {

    @Override
    public String getRegexPattern() {
        return "![Bb]eocat start .*|![Bb]eocat go .*|![Bb]eocat look .*|![Bb]eocat examine .*|![Bb]eocat take .*|![Bb]eocat drop .*|![Bb]eocat kill .*|![Bb]eocat wait|![Bb]eocat wear .*|![Bb]eocat use .*|![Bb]eocat help .*|![Bb]eocat inventory .*|![Bb]eocat save .*|![Bb]eocat load .*|![Bb]eocat reset .*|![Bb]eocat quit .*";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public String getInfo(){
        return "Beocat Break-In";
    }

    @Override
    public String[] getCommands(){
        String[] commands = {
            "!beocat start <gameName> - Start a new Beocat Break-In game.",
            "!beocat help - List the commands for the game."
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel) {

        String val = message.toLowerCase().substring(8);
        if(message.toLowerCase().startsWith("!beocat start")){

            String command = val.substring(6);
            //start game
            Session.getInstance().sendMessage("Started a new game called" + command + ".", channel);
          }
          else if(message.startsWith("!beocat go")) {

            String command = val.substring(3);
            Session.getInstance().sendMessage("Moved " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat look")) {

            String command = val.substring(5);
            Session.getInstance().sendMessage("Looked at " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat examine")) { //start test

            String command = val.substring(8);
            Session.getInstance().sendMessage("You examine the " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat take")) {

            String command = val.substring(5);
            Session.getInstance().sendMessage("You take the " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat drop")) {

            String command = val.substring(5);
            Session.getInstance().sendMessage("You drop the " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat kill")) {

            String command = val.substring(5);
            Session.getInstance().sendMessage("You have died." + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat wait")) {

            Session.getInstance().sendMessage("You are waiting around.", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat wear")) {

            String command = val.substring(5);
            Session.getInstance().sendMessage("You put on the " + command + ".", channel);
          }
          else if(message.toLowerCase().startsWith("!beocat use")) {

            String[] items = val.substring(4).split(" ");
            Session.getInstance().sendMessage("You use " + items[0] + " on " + items[1] + ".", channel);
          }

    }



}
