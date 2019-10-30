package xyz.kazuthecat.coffeebot.settings;

import xyz.kazuthecat.coffeebot.DBHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsDB extends SettingsAbstract {
    private final DBHandler dbHandler;
    private boolean dbFunctional;

    /**
     * The main constructor for the class.
     * @param dbHandler A DBHandler object that can read and write to a database.
     */
    public SettingsDB(DBHandler dbHandler) {
        this.dbHandler = dbHandler;

        // Create settings table if absent in database.
        String settingsDB = "CREATE TABLE IF NOT EXISTS settingdb(" +
                "id       VARCHAR(30) NOT NULL, " +
                "name     VARCHAR(30) NOT NULL, " +
                "value    TEXT NOT NULL, " +
                "PRIMARY KEY (id, name) );";
        this.dbFunctional = dbHandler.execute(new String[]{settingsDB});

        // Load settings table from database.
        List<Map<String, String>> loadedSettings = null;
        if (this.dbFunctional) {
            loadedSettings = dbHandler.query("SELECT * FROM settingdb;");
            this.dbFunctional = loadedSettings != null;
        }

        // Map settings table to customSettings (Map<String, Map<String, String>>);
        if (this.dbFunctional) {
            for (Map<String, String> setting : loadedSettings) {
                String name = setting.get("name");
                String owner = setting.get("id");
                String value = setting.get("value");

                if (!customSettings.containsKey("name")) { customSettings.put(name, new HashMap<>()); }
                Map<String, String> customSetting = customSettings.get(name);
                customSetting.put(owner, value);
            }
        }
    }

    /**
     * This version of Settings will write the changes to a database and only updates the relevant setting, it is
     * thus much more efficient than SettingsFile. The downside to this is that it does require a MySQL server to
     * run. Another potential downside is latency if the MySQL server is located off site.
     *
     * Furthermore, failure to write to the database is not reported to the end user and will never be corrected by
     * the bot. This shouldn't happen if the MySQL server is set up properly, however if at any point it becomes
     * unreachable the changes to the settings will be reverted once the bot is restarted.
     * @param settingName The name of the setting that's affected.
     * @param id The ID of the user or guild whose settings are being changed (or DEFAULT for owner settings).
     * @param value The new value for the setting.
     */
    @Override
    void writeJSON(String settingName, String id, String value) {
        // Value is null if a setting has been deleted.
        if (value != null) {
            String sql =
                    "INSERT INTO settingdb (id, name, value) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE value = ?;";
            dbHandler.execute(sql, new String[][]{{id, settingName, value, value}});
        } else {
            String sql = "DELETE FROM settingdb WHERE id = ? AND name = ?;";
            dbHandler.execute(sql, new String[][]{{id, settingName}});
        }

    }
}
