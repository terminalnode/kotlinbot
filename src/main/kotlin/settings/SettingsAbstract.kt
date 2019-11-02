package xyz.kazuthecat.coffeebot.settings

import net.dv8tion.jda.api.entities.*
import java.util.HashMap
import java.util.stream.Collectors

abstract class SettingsAbstract {
  private val defaultSettings = HashMap<String, String>()
  private val userChangeable = HashMap<String, Boolean>()
  private val adminChangeable = HashMap<String, Boolean>()
  internal var customSettings = HashMap<String, MutableMap<String, String>>()

  internal abstract fun writeJSON(settingName: String, id: String, value: String?)

  fun setDefaults(settingName: String, defaultValue: String, userChangeable: Boolean, adminChangeable: Boolean) {
    defaultSettings[settingName] = defaultValue
    this.userChangeable[settingName] = userChangeable
    this.adminChangeable[settingName] = adminChangeable
    customSettings.computeIfAbsent(settingName) { _ -> HashMap()}
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
    return when {
      userChangeable[settingName] == null -> SettingEnum.DOESNOTEXIST
      userChangeable[settingName]!! -> removeSetting(user.id, settingName)
      else -> SettingEnum.FORBIDDEN
    }
  }

  fun removeSetting(guild: Guild, settingName: String) : SettingEnum {
    return when {
      userChangeable[settingName] == null -> SettingEnum.DOESNOTEXIST
      userChangeable[settingName]!! -> removeSetting(guild.id, settingName)
      else -> SettingEnum.FORBIDDEN
    }
  }

  fun removeSetting(settingName: String) : SettingEnum {
    return when {
      defaultSettings[settingName] == null -> SettingEnum.DOESNOTEXIST
      else -> removeSetting("DEFAULT", settingName)
    }
  }

  private fun removeSetting(owner : String, settingName: String) : SettingEnum {
    println("Removing setting $settingName for $owner.")
    if (!customSettings[settingName]?.containsKey(owner)!!) {
      println("Nevermind it wasn't set")
      return SettingEnum.NOTSET
    }

    return try {
      customSettings[settingName]?.remove(owner)
      writeJSON(settingName, owner, null)
      SettingEnum.SUCCESSFUL
    } catch (_ : Exception) {
      SettingEnum.ERROR
    }
  }

  fun getSetting(settingName: String, message: Message) : String {
    val defaultSetting = defaultSettings[settingName] ?: "This response is broken :("
    val setting = customSettings[settingName] ?: return standardReplacements(defaultSetting, message)

    // Return user setting if we have one
    val userSetting = setting[message.author.id]
    if (userSetting != null) { return standardReplacements(userSetting, message) }

    // Return guild setting if message is from guild and we have one
    if (message.isFromGuild) {
      val guildSetting = setting[message.guild.id]
      if (guildSetting != null) { return standardReplacements(guildSetting, message) }
    }

    // Return altered default value if we have one
    val customSetting = setting["DEFAULT"]
    if (customSetting != null) { return standardReplacements(customSetting, message) }

    // Fall back to returning the default value if we can't find any of the above
    return standardReplacements(defaultSetting, message)
  }

  private fun standardReplacements(input : String, message: Message) : String {
    return input
      .replace("\\{(user|author)\\}".toRegex(), message.author.asMention)
      .replace("\\{(bot|botname|coffeebro)\\}".toRegex(), message.jda.selfUser.asMention)
      .replace("\\{(message|content)\\}".toRegex(), message.contentRaw.trim())
  }

  fun allSettingsContaining(substring : String) : Set<String> {
    return customSettings
      .keys
      .stream()
      .filter { x -> x.contains(substring) }
      .collect(Collectors.toSet())
  }
}