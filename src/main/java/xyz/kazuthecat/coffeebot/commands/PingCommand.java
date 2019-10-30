package xyz.kazuthecat.coffeebot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import xyz.kazuthecat.coffeebot.settings.SettingsAbstract;

/* This function is a sort of demo function where all the various parts of the command
 * class and important syntax stuff (mostly lambda functions) are explained for future reference. */
public class PingCommand extends Command {
    private final SettingsAbstract settings;
    private final String pingSetting;

    public PingCommand(SettingsAbstract settings) {
        /* this.name/help/guildOnly are values used for various functions and for the
           most part self explanatory. guildOnly makes it not work in DMs, help is
           printed in the help menu, name is the name used in help menus and when
           invoking the command (we can also set aliases) */
        this.name = "ping";
        this.help = "pings the bot";
        this.guildOnly = false;

        /* Settings object is used to store defaults as well as retrieve settings.
         * Here we tell it what settings we want it to keep track of, and their default values. */
        this.settings = settings;
        this.pingSetting = "ping.response";
        settings.setDefaults(pingSetting, "Pong!", true, true);
    }

    /* The execute function is part of the Command class and entails the events
     * that occur when the command is triggered. */
    @Override
    protected void execute(CommandEvent event) {
        // Retrieving setting for what the response message should be.
        String pingResponse = settings.getSetting(pingSetting, event.getMessage());

        long time = System.currentTimeMillis();
        event.getChannel().sendMessage(pingResponse)
                .queue(response -> response.editMessageFormat(
                        pingResponse + " %d ms",
                        (System.currentTimeMillis() - time))
                        .queue());
    }
}
