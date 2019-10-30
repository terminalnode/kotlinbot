package xyz.kazuthecat.coffeebot.commands.setcommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import xyz.kazuthecat.coffeebot.settings.SettingEnum;
import xyz.kazuthecat.coffeebot.settings.SettingsAbstract;

public class UnSetGuildCommand extends Command {
    private final SettingsAbstract settings;

    public UnSetGuildCommand(SettingsAbstract settings) {
        this.name = "unsetserver";
        this.help = "unsets a setting for the current guild/server";
        this.aliases = new String[]{"guildunset","unsetguild","serverunset"};
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.arguments = "settingName";
        this.guildOnly = true;
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
            SettingEnum settingStatus = settings.removeSetting(event.getGuild(), settingName);
            switch (settingStatus) {
                case SUCCESSFUL:
                    reply = " The setting has been reset!"; break;
                case DOESNOTEXIST:
                    reply = " There is no setting by the name of **" + settingName + "**, check your spelling or something idk."; break;
                case FORBIDDEN:
                    reply = " That setting can not be changed at a server level."; break;
                case NOTSET:
                    reply = " That setting isn't set for this server, so I guess you can consider it unset?"; break;
                default:
                    // Includes SettingEnum.ERROR
                    reply = " Something went wrong. Not sure what. Not sure I care."; break;
            }
        }

        event.getChannel().sendMessage(event.getAuthor().getAsMention() + reply).queue();
    }
}
