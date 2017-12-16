package russbot.plugins;
import russbot.Session;
import russbot.Storage;

import java.util.AbstractMap;
import java.util.Base64;

public class ReggiesQuest extends Plugin {

    private AbstractMap<String, String> data;

    public ReggiesQuest(){
        data = Storage.getMap("scorekeeping");
    }

    @Override
    public String getRegexPattern() {
        return "!reggie";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test"};
        return channels;
    }

    @Override
    public String getInfo() {
        return "help Reggie find his way back home!";
    }

    @Override
    public String[] getCommands() {
        String[] commands = {
                "!reggie - start/load a game of _Reggie's Quest_",
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel, String username, String userid) {
        if(username.equals("russfeld") || username.equals("russfeldh")) {
            String newChannel = Session.getInstance().registerPrivateChannel(new String[]{username}, this);
            StringBuilder output = new StringBuilder();
            printWelcomeMenu(output);
            Session.getInstance().sendPrivateMessage(output.toString(), newChannel);
        }else{
            Session.getInstance().sendMessage("You aren't my creator...you'll have to wait and see what this does!", channel);
        }
    }

    @Override
    public void privateMessagePosted(String message, String channel, String username, String userid){
        //Important Variables
        byte[] state;
        StringBuilder output = new StringBuilder();
        boolean disconnect = false;
        boolean step = false;

        //Load state from data
        if(data.containsKey(userid)){
            state = Base64.getDecoder().decode(data.get(userid));
        }else{
        //Create new state
            state = new byte[1];
            state[0] = 0;
        }

        String[] input = message.split(" ");

        input[0] = input[0].toLowerCase();

        //Single word commands
        if(input.length == 1){
            switch(input[0]){
                case "new":
                    state = new byte[1];
                    state[0] = 0;
                    step = true;
                    break;
                case "continue":
                    if(state[0] == 0){
                        output.append("_No saved game found on server!_");
                    }else{
                        output.append("_Saved game loaded from server! Continuing..._\n");
                        step = true;
                    }
                    break;
                case "save":
                    output.append("_You can resume your game from this state at any time using the following code:_\n");
                    output.append("`" +  Base64.getEncoder().encodeToString(state) +"`");
                    break;
                case "menu":
                    printWelcomeMenu(output);
                    break;
                case "quit":
                    disconnect = true;
                    output.append("_Thanks for playing!_");
                    break;
                case "yes":
                    state = doAction(output, state, input[0], null);
                    step = true;
                    break;
                case "!help":
                case "!commands":
                case "!about":
                    state = doAction(output, state, "help", null);
                    step = true;
                    break;
                default:
                    printError(output);
                    break;
            }
        }

        //Multiple word commands
        if(input.length == 2){
            switch(input[0]){
                case "load":
                    try{
                        state = Base64.getDecoder().decode(input[1]);
                        output.append("_State loaded!_\n");
                        step = true;
                    }catch(IllegalArgumentException e){
                        output.append("_Unable to load state! Error:_\n");
                        output.append("_"+ e.getMessage() + "_");
                    }
                    break;
                default:
                    printError(output);
                    break;
            }
        }

        if(step){
            state = step(output, state);
        }

        //Save state
        data.put(userid, Base64.getEncoder().encodeToString(state));

        Session.getInstance().sendPrivateMessage(output.toString(), channel);

        if(disconnect){
            Session.getInstance().unregisterPrivateChannel(channel);
        }
    }

    private void printWelcomeMenu(StringBuilder output){
        output.append("*Welcome to _Reggie's Quest_!*\n");
        output.append("`new` - _Start a new game_\n");
        output.append("`continue` - _Continue a game (if available)_\n");
        output.append("`load <code>` - _Load a saved game from a code_\n");
        output.append("`save` - _Save your game to a code_\n");
        output.append("`menu` - _Display this menu_\n");
        output.append("`quit` - _Quit Reggie's Quest_\n");
    }

    private void printError(StringBuilder output){
        output.append("_I don't recognize that command. Type_ `help` _or_ `menu` _for available commands._");
    }

    private byte[] step(StringBuilder output, byte[] state){
        switch(state[0]){
            case 0:
                output.append("~Generic welcome message here.~");
                break;
            case 1:
                output.append("~You have begun the quest.~");
                break;
        }
        return state;
    }

    private byte[] doAction(StringBuilder output, byte[] state, String action, String target){
        switch(action){
            case "yes":
                switch(state[0]){
                    case 0:
                        output.append("_You nod your head in agreement._\n");
                        state[0] = 1;
                        break;
                    default:
                        output.append("_You nod your head vigorously, but nothing happens._\n");
                        break;
                }
                break;
            default:
                printError(output);
                break;
        }
        return state;
    }
}

