package russbot.plugins;
import russbot.Session;

public class ReggiesQuest extends Plugin {

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
        String newChannel = Session.getInstance().registerPrivateChannel(new String[]{username}, this);
        Session.getInstance().sendPrivateMessage("Welcome to _Reggie's Quest_!", newChannel);
    }
}

