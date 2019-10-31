package xyz.kazuthecat.coffeebot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import xyz.kazuthecat.coffeebot.settings.SettingsAbstract

class PingCommand constructor(private val settings: SettingsAbstract): Command() {
  private val pingSetting : String = "ping.response"

  init {
    name = "ping"
    help = "pings the bot"
    guildOnly = false
    settings.setDefaults(pingSetting, "Pong!", true, true)
  }

  override fun execute(event: CommandEvent?) {
    if (event == null) { return }

    val pingResponse = settings.getSetting(pingSetting, event.message)
    val time = System.currentTimeMillis()
    event.channel
      .sendMessage(pingResponse)
      .queue {
        response -> response
          .editMessage("$pingResponse ${System.currentTimeMillis() - time} ms")
          .queue()
      }
  }
}