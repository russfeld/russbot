package russbot.plugins;
import russbot.Session;

public class DirectMessage extends Plugin {

    @Override
    public String getRegexPattern() {
        return "!dm";
    }

    @Override
    public String[] getChannels() {
        String[] channels = {"test", "random", "general"};
        return channels;
    }

    @Override
    public String getInfo() {
        return "sample code for direct messages with russbot";
    }

    @Override
    public String[] getCommands() {
        String[] commands = {
                "!dm - start a direct message session with russbot",
        };
        return commands;
    }

    @Override
    public void messagePosted(String message, String channel, String username, String userid) {
        String newChannel = Session.getInstance().registerPrivateChannel(new String[]{username}, this);
        Session.getInstance().sendPrivateMessage("Hello <@" + userid + ">!", newChannel);
    }

    @Override
    public void privateMessagePosted(String message, String channel, String username, String userid){
        Session.getInstance().sendPrivateMessage("I'm not very talkative! (Seriously, that's all this plugin does)", channel);
    }
}

