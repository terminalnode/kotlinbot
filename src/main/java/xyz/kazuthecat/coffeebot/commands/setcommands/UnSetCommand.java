package xyz.kazuthecat.coffeebot.commands.setcommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import xyz.kazuthecat.coffeebot.settings.SettingEnum;
import xyz.kazuthecat.coffeebot.settings.SettingsAbstract;

public class UnSetCommand extends Command {
    private final SettingsAbstract settings;

    public UnSetCommand(SettingsAbstract settings) {
        this.name = "unset";
        this.help = "unsets a setting for the current user";
        this.arguments = "settingName";
        this.guildOnly = false;
        this.category = new Category("Settings");

        // Set settings
        this.settings = settings;
    }

    @Override
    protected void execute(CommandEvent event) {
        String settingName = event.getArgs().split(" ")[0];
        String reply;

        if (settingName.isBlank()) {
            reply = " You need to specify a setting *and* a value for that setting, DOLT!";
        } else {
            SettingEnum settingStatus = settings.removeSetting(event.getAuthor(), settingName);
            switch (settingStatus) {
                case SUCCESSFUL:
                    reply = " The setting has been reset!"; break;
                case DOESNOTEXIST:
                    reply = " There is no setting by the name of **" + settingName + "**, check your spelling or something idk."; break;
                case NOTSET:
                    reply = " That setting isn't set for you, so I guess you can consider it unset?"; break;
                default:
                    // Includes SettingEnum.ERROR
                    reply = " Something went wrong. Not sure what. Not sure I care."; break;
            }
        }

        event.getChannel().sendMessage(event.getAuthor().getAsMention() + reply).queue();
    }
}
