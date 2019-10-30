package xyz.kazuthecat.coffeebot.settings;

/**
 * An enum used to indicate whether an operation pertaining to settings were successful or not.
 */
public enum SettingEnum {
    SUCCESSFUL,   // Operation carried out with no errors.
    FORBIDDEN,    // The setting is not allowed to be changed at user/server level.
    DOESNOTEXIST, // The setting in question does not exist.
    NOTSET,       // When someone tries to reset a setting that is not set.
    ERROR         // Some unexpected error occurred.
}
