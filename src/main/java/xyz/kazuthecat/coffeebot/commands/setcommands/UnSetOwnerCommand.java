package xyz.kazuthecat.coffeebot.commands.setcommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import xyz.kazuthecat.coffeebot.settings.SettingEnum;
import xyz.kazuthecat.coffeebot.settings.SettingsAbstract;

public class UnSetOwnerCommand extends Command {
    private final SettingsAbstract settings;

    public UnSetOwnerCommand(SettingsAbstract settings) {
        this.name = "unsetowner";
        this.help = "changes the default value for a setting";
        this.aliases = new String[]{"ownerunset", "unsetbot", "botunset"};
        this.arguments = "settingName value";
        this.guildOnly = false;
        this.ownerCommand = true;
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
            SettingEnum settingStatus = settings.removeSetting(settingName);
            switch (settingStatus) {
                case SUCCESSFUL:
                    reply = " The setting has been reset!"; break;
                case DOESNOTEXIST:
                    reply = " There is no setting by the name of **" + settingName + "**, check your spelling or something idk."; break;
                case NOTSET:
                    reply = " The default for that setting hasn't been altered. So... mission accomplished?"; break;
                default:
                    // Includes SettingEnum.ERROR
                    reply = " Something went wrong. Not sure what. Not sure I care."; break;
            }
        }
        event.reply(reply);
    }
}
