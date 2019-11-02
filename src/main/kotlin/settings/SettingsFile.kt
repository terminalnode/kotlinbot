package xyz.kazuthecat.coffeebot.settings

import com.google.gson.Gson

class SettingsFile : SettingsAbstract() {
  private val gson = Gson()

  override fun writeJSON(settingName: String, id: String, value: String?) {
    println("Wanted to write JSON to file but SettingsFile.writeJSON() isn't implemented yet!")
  }
}