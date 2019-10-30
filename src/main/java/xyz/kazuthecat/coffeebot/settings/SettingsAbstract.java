package xyz.kazuthecat.coffeebot.settings;

import com.google.gson.Gson;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class contains all the main setting functions shared by it's subclasses. It has only one abstract method
 * called writeJSON which is dictates how settings are saved, while loading of settings from this medium is handled
 * by the subclasses constructor methods.
 */
public abstract class SettingsAbstract {
    final Gson gson = new Gson();
    private final Map<String, String> defaultSettings = new HashMap<>();
    private Map<String, Boolean> userChangeable = new HashMap<>();
    private Map<String, Boolean> adminChangeable = new HashMap<>();
    Map<String, Map<String, String>> customSettings = new HashMap<>();

    /**
     * This method dictates how settings are saved for long-term storage. For SettingsDB this means writing the setting
     * to a MySQL database server and for SettingsFile this means writing all settings to disk in JSON format.
     *
     * The parameters are not used by all subclasses. Since SettingsFile dumps all settings into a file the arguments
     * passed to the function are ignored, however SettingsDB will only write one setting at a time. Including the
     * parameters for this method allows for maximum flexibility in the subclasses on how exactly to save the data.
     * @param settingName The name of the setting that's affected.
     * @param id The ID of the user or guild whose settings are being changed (or DEFAULT for owner settings).
     * @param value The new value for the setting.
     */
    abstract void writeJSON(String settingName, String id, String value);

    /**
     * This function is called by each command that wants to add it's settings. Each call creates
     * an entry in defaultSettings, and in customSettings if there were none at initialization.
     * Finally, userChangeable and adminChangeable are set.
     * @param settingName The name of the setting, e.g. something like hello.response.
     * @param defaultValue The default value for the setting (unless one was loaded previously).
     * @param userChangeable Boolean saying whether users are allowed to change this setting.
     * @param adminChangeable Boolean saying whether server admins are allowed to change this setting.
     */
    public void setDefaults(String settingName, String defaultValue, boolean userChangeable, boolean adminChangeable) {
        defaultSettings.put(settingName, defaultValue);
        customSettings.computeIfAbsent(settingName, k -> new HashMap<>());
        this.userChangeable.put(settingName, userChangeable);
        this.adminChangeable.put(settingName, adminChangeable);
    }

    /**
     * Method for changing a setting for an individual user.
     * @param user The User object for the user.
     * @param settingName The name of the setting.
     * @param value The new value for the setting.
     * @return A SettingEnum indicating whether the operation was successful.
     */
    public SettingEnum putSetting(User user, String settingName, String value) {
        if (!userChangeable.get(settingName)) {
            return SettingEnum.FORBIDDEN;
        } else {
            return putSetting(user.getId(), settingName, value);
        }
    }

    /**
     * Method for changing a setting for a specific guild.
     * @param guild The Guild object for the guild.
     * @param settingName The name of the setting.
     * @param value The new value for the setting.
     * @return A SettingEnum indicating whether the operation was successful.
     */
    public SettingEnum putSetting(Guild guild, String settingName, String value) {
        if (!userChangeable.get(settingName)) {
            return SettingEnum.FORBIDDEN;
        } else {
            return putSetting(guild.getId(), settingName, value);
        }
    }

    /**
     * Method for setting a new default value for a setting. This value will be used in all servers as long
     * as users don't have their own settings or admins have set a guild setting.
     * @param settingName The name of the setting.
     * @param value The new value for the setting.
     * @return A SettingEnum indicating whether the operation was successful.
     */
    public SettingEnum putSetting(String settingName, String value) {
        return putSetting("DEFAULT", settingName, value);
    }

    /**
     * Helper method called by the other putSetting functions.
     * This does all the "heavy lifting" when setting a new setting.
     * @param id The ID of the user or guild whose settings are being changed (or "DEFAULT" for owner settings).
     * @param settingName The name of the setting that's being changed.
     * @param value The new value for the setting.
     * @return A SettingEnum indicating if the operation was successful.
     */
    private SettingEnum putSetting(String id, String settingName, String value) {
        if (!defaultSettings.containsKey(settingName)) {
            return SettingEnum.DOESNOTEXIST;
        } else {
            customSettings.get(settingName).put(id, value);
            writeJSON(settingName, id, value);
            return SettingEnum.SUCCESSFUL;
        }
    }

    /**
     * Method for resetting a setting for an individual user.
     * @param user The User object for the user.
     * @param settingName The name of the setting.
     * @return A SettingEnum indicating whether the operation was successful.
     */
    public SettingEnum removeSetting(User user, String settingName) {
        if (!userChangeable.get(settingName)) {
            return SettingEnum.FORBIDDEN;
        } else {
            return removeSetting(user.getId(), settingName);
        }
    }

    /**
     * Method for resetting a setting for a specific guild.
     * @param guild The Guild object for the guild.
     * @param settingName The name of the setting.
     * @return A SettingEnum indicating whether the operation was successful.
     */
    public SettingEnum removeSetting(Guild guild, String settingName) {
        if (!userChangeable.get(settingName)) {
            return SettingEnum.FORBIDDEN;
        } else {
            return removeSetting(guild.getId(), settingName);
        }
    }

    /**
     * Method for resetting a changed default value to the default provided by the command itself.
     * @param settingName The name of the setting.
     * @return A SettingEnum indicating whether the operation was successful.
     */
    public SettingEnum removeSetting(String settingName) {
        return removeSetting("DEFAULT", settingName);
    }

    /**
     * Helped method used by the other removeSetting functions to change the setting.
     * @param id The ID of the user or guild whose setting is being removed (or DEFAULT for owner settings).
     * @param settingName The name of the setting that's being removed.
     * @return A SettingEnum indicating if the operation was successful.
     */
    private SettingEnum removeSetting(String id, String settingName) {
        if (!defaultSettings.containsKey(settingName)) {
            return SettingEnum.DOESNOTEXIST;
        } else {
            Map<String, String> setting = customSettings.get(settingName);
            if (setting.containsKey(id)) {
                customSettings.get(settingName).remove(id);
                writeJSON(settingName, id, null);
                return SettingEnum.SUCCESSFUL;
            } else {
                return SettingEnum.NOTSET;
            }
        }
    }

    /**
     * Method for retrieving the most relevant setting for responding to a message.
     * If a user setting is set (and the setting is currently userChangeable), that setting is returned.
     * If a guild setting is set (and the setting is currently adminChangeable), that setting is returned.
     * If the default setting has been altered, that setting is returned.
     * If the setting has not been altered, the default setting is returned.
     * @param settingName The name of the setting.
     * @param message The message requesting the setting.
     * @return A string containing the setting.
     */
    public String getSetting(String settingName, Message message) {
        String botSetting = null, userSetting = null, guildSetting = null, result;
        Map<String, String> setting = customSettings.get(settingName);

        if (setting != null) {
            if (userChangeable.get(settingName))
                userSetting = setting.get(message.getAuthor().getId());
            if (adminChangeable.get(settingName) && message.isFromGuild())
                guildSetting = setting.get(message.getGuild().getId());
            botSetting = setting.get("DEFAULT");
        }

        if (userSetting != null) {
            result = standardReplacements(userSetting, message);
        } else if (guildSetting != null) {
            result = standardReplacements(guildSetting, message);
        } else if (botSetting != null) {
            result = standardReplacements(botSetting, message);
        } else {
            result = standardReplacements(defaultSettings.get(settingName), message);
        }

        return result;
    }

    /**
     * Method for carrying out a few standard replacements on a retrieved setting.
     * Placeholders are indicated with {brackets}.
     * @param input The string we're doing replacements on.
     * @param message The message requesting the setting.
     * @return A modified version of input with all replacements carried out.
     */
    private String standardReplacements(String input, Message message) {
        return input
                .replaceAll("\\{user\\}", message.getAuthor().getAsMention())
                .replaceAll("\\{botname\\}", message.getJDA().getSelfUser().getAsMention())
                .replaceAll("\\{content\\}", message.getContentRaw().strip());
    }

    /**
     * Method for searching through the settings by looking for setting names containing the provided substring.
     * @param substring The substring we're filtering settings based on.
     * @return A set of setting names (String) whose names contain the provided substring.
     */
    public Set<String> allSettingsContaining(String substring) {
        return customSettings
                .keySet()
                .stream()
                .filter(x -> x.contains(substring))
                .collect(Collectors.toSet());
    }
}
