package xyz.kazuthecat.coffeebot.settings;

// import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * A subclass to SettingsAbstract that saves all settings to disk in JSON format.
 * The benefit of this is that no additional setup is required, the downside is that all settings
 * are written to disk every time a setting is change. For many settings this can result in a lot
 * of disk I/O.
 */
public class SettingsFile extends SettingsAbstract {
    private final String settingsFile;

    /**
     * The main constructor for the settings class.
     */
    public SettingsFile() {
        settingsFile = "botsettings.json";
        customSettings = null;
        try {
            Path settingsPath = Paths.get(settingsFile);
            String json = Files.readString(settingsPath);
            Type settingsMapType = new TypeToken<HashMap<String, Map<String, String>>>() {}.getType();
            customSettings = gson.fromJson(json, settingsMapType);
        } catch (Exception e) {
            System.out.println("Something went wrong when trying to load json.");
        }

        if (customSettings == null) {
            customSettings = new HashMap<>();
        }
    }

    /**
     * This version of Settings will dump all settings into a file in JSON format, allowing for a simple solution with
     * zero configuration. The downside to this is that it might lead to a lot of disk I/O, slowing down the bot's
     * performance and potentially wearing out the disk.
     * @param settingName The name of the setting that's affected.
     * @param id The ID of the user or guild whose settings are being changed (or DEFAULT for owner settings).
     * @param value The new value for the setting.
     */
    @Override
    void writeJSON(String settingName, String id, String value) {
        // Pretty print json, for debugging
        // System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(customSettings));

        String json = gson.toJson(customSettings);
        try {
            PrintStream out = new PrintStream(new FileOutputStream(settingsFile));
            out.print(json);
            out.flush();
        } catch (Exception e) {
            System.out.println("Error: Failed to write " + settingsFile + " to disk:");
            System.out.println(e.toString());
        }
    }

}
