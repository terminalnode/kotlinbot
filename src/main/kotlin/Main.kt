import java.nio.file.Files
import java.nio.file.Paths
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import xyz.kazuthecat.coffeebot.DBHandler
import xyz.kazuthecat.coffeebot.commands.CoffeeLog
import xyz.kazuthecat.coffeebot.commands.PingCommand
import xyz.kazuthecat.coffeebot.commands.setcommands.*
import xyz.kazuthecat.coffeebot.listeners.HelloListener
import xyz.kazuthecat.coffeebot.settings.SettingsDB

fun main() {
  // LOAD CONFIG
  // Config file should be placed in project root.
  // Line #1 = bot token, line #2 = owner ID
  // Lines #3-#5 should be address, username and password to the database.
  val configFile = Files.readAllLines(Paths.get("config"))
  val token = configFile[0].trim()
  val ownerID = configFile[1].trim()
  val dbAddr = configFile[2].trim()
  val dbUser = configFile[3].trim()
  val dbPass = configFile[4].trim()

  // BUILD CLIENT
  val client = CommandClientBuilder()
  client.setOwnerId(ownerID)
  client.setPrefix("?")
  client.setStatus(OnlineStatus.DO_NOT_DISTURB)
  client.setActivity(Activity.playing("with Kotlin"))
  client.setEmojis("☕", "\uD83D\uDE92", "\uD83D\uDD25")

  val dbHandler = DBHandler(dbAddr, dbUser, dbPass)
  val settings = SettingsDB(dbHandler)
  val eventWaiter = EventWaiter()

  client.addCommands(
    PingCommand(settings),
    CoffeeLog(dbHandler),

    // Settings functions
    SetCommand(settings),
    UnSetCommand(settings),
    SetGuildCommand(settings),
    UnSetGuildCommand(settings),
    SetOwnerCommand(settings)
  )

  JDABuilder(AccountType.BOT)
    .setToken(token)
    .addEventListeners(
      // Add our own listeners
      HelloListener(eventWaiter, settings),

      // Build the command client from JDA Utilities
      client.build()
    ).build()
}