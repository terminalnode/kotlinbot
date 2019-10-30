# Kotlin Bot
A Kotlin-translation of [Coffee Bot](https://github.com/terminalnode/coffeebot), gradually translating all parts of the bot into Kotlin while maintaining functionality thanks to Kotlin and Java interoperability.

This is a work in progress. At time of writing only the main class has migrated to Kotlin and HelloListener seem to be broken.

## Functionality
Some novelty functions developed in order to test functionality, nothing particularily useful. In its current form it mostly serves as a canvas for more elaborate projects.

### Persistent user/guild/owner settings
Any class can be handed an instance of the Settings-class to add and retrieve it's own settings, with a few possibilities to add wild cards such as `{user}` or `{botname}` for the bot and user names respectively. Settings are stored and retrieved from memory through methods defined in SettingsAbstract, however the subclasses SettingsDB and SettingsFile will also make persistent saves of settings that are loaded when the bot starts up. SettingsDB accomplishes this by saving settings to a MySQL database and SettingsFile by writing them to file in JSON format.

### Easy to use MySQL support
The class DBHandler can connect to a MySQL database, execute statements and queries, and return the result of queries as a HashMap list. For example usage look at settings/SettingsDB and commands/Coffeelog.

## Usage
The main method of the Main class which initalizes the bot reads certain settings from a file called `config` in project root, for more information on what this file should contain consult the source code of this file.

Using a MySQL database is not required for most functions, but it is required for SettingsDB and coffeelog.
