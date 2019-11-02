package xyz.kazuthecat.coffeebot.settings

import xyz.kazuthecat.coffeebot.DBHandler

class SettingsDB(dbHandler: DBHandler) : SettingsAbstract() {
  init {
    println("Size of customSettings: ${customSettings.size}")
  }
  override fun writeJSON(settingName: String, id: String, value: String?) {
    println("Wanted to write JSON to file but SettingsDB.writeJSON() isn't implemented yet!")
  }
}