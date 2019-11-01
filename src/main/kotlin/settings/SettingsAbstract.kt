package xyz.kazuthecat.coffeebot.settings

import com.google.gson.Gson
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import java.util.HashMap

abstract class SettingsAbstract {
  internal val gson = Gson()
  private val defaultSettings = HashMap<String, String>()
  private val userChangeable = HashMap<String, Boolean>()
  private val adminChangeable = HashMap<String, Boolean>()
  internal var customSettings = HashMap<String, MutableMap<String, String>>()

  init {
    // No init
  }

  internal abstract fun writeJSON(settingName: String, id: String, value: String?)

  fun setDefaults(settingName: String, defaultValue: String, userChangeable: Boolean, adminChangeable: Boolean) {
    defaultSettings[settingName] = defaultValue
    this.userChangeable[settingName] = userChangeable
    this.adminChangeable[settingName] = adminChangeable

    // TODO replace with computeIfAbsent thing or getOrPut
    // if (customSettings[settingName] == null) {
    //   customSettings[settingName] = HashMap()
    // }
    customSettings.computeIfAbsent(settingName) { _ -> HashMap()}
    println(customSettings.size)
  }


  fun putSetting(user : User, settingName : String, value : String) : SettingEnum {
    return when {
      userChangeable[settingName] == null -> SettingEnum.DOESNOTEXIST
      userChangeable[settingName]!! -> putSetting(user.id, settingName, value)
      else -> SettingEnum.FORBIDDEN
    }
  }

  fun putSetting(guild : Guild, settingName: String, value: String) : SettingEnum {
    return when {
      userChangeable[settingName] == null -> SettingEnum.DOESNOTEXIST
      userChangeable[settingName]!! -> putSetting(guild.id, settingName, value)
      else -> SettingEnum.FORBIDDEN
    }
  }

  fun putSetting(settingName: String, value: String) : SettingEnum {
    return when {
      defaultSettings[settingName] == null -> SettingEnum.DOESNOTEXIST
      else -> putSetting("DEFAULT", settingName, value)
    }
  }

  private fun putSetting(owner : String, settingName: String, value: String) : SettingEnum {
    println("Changing setting $settingName for $owner to $value")
    return try {
      customSettings[settingName]?.set(owner, value)
      writeJSON(settingName, owner, value)
      SettingEnum.SUCCESSFUL
    } catch (_ : Exception) {
      SettingEnum.ERROR
    }
  }

  fun removeSetting(user : User, settingName: String) : SettingEnum {
    return removeSetting(user.id, settingName)
  }

  fun removeSetting(guild: Guild, settingName: String) : SettingEnum {
    return removeSetting(guild.id, settingName)
  }

  fun removeSetting(settingName: String) : SettingEnum {
    return removeSetting("DEFAULT", settingName)
  }

  private fun removeSetting(owner : String, settingName: String) : SettingEnum {
    return SettingEnum.ERROR
  }

  fun getSetting(settingName: String, message: Message) : String {
    return "blabla"
  }

  private fun standardReplacements(input : String, message: Message) : String {
    return input
  }

  fun allSettingsContaining(substring : String) : Set<String> {
    return HashSet()
  }
}