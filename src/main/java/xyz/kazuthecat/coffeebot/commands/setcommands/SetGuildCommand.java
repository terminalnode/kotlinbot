package xyz.kazuthecat.coffeebot.commands.setcommands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import xyz.kazuthecat.coffeebot.settings.SettingEnum;
import xyz.kazuthecat.coffeebot.settings.SettingsAbstract;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SetGuildCommand extends Command {
    private final SettingsAbstract settings;

    public SetGuildCommand(SettingsAbstract settings) {
        this.name = "setserver";
        this.help = "sets a setting for the current guild/server";
        this.aliases = new String[]{"guildset","setguild","serverset"};
        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};
        this.arguments = "settingName value";
        this.guildOnly = true;
        this.category = new Category("Settings");

        // Set settings
        this.settings = settings;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] arglist = event.getArgs().split(" ");
        String settingName = arglist[0];
        String value = Arrays.stream(arglist).skip(1).collect(Collectors.joining(" "));
        String reply;

        if (value.isBlank()) {
            reply = " You need to specify a setting *and* a value for that setting, DOLT!";
        } else {
            SettingEnum settingStatus = settings.putSetting(event.getGuild(), settingName, value);
            switch (settingStatus) {
                case SUCCESSFUL:
                    reply = " The server settings have been updated!"; break;
                case DOESNOTEXIST:
                    reply = " There is no setting by the name of **" + settingName + "**, check your spelling or something idk."; break;
                case FORBIDDEN:
                    reply = " That setting can not be changed at a server level."; break;
                default:
                    // Includes SettingEnum.ERROR
                    reply = " Something went wrong. Not sure what. Not sure I care."; break;
            }
        }

        event.getChannel().sendMessage(event.getAuthor().getAsMention() + reply).queue();
    }
}
