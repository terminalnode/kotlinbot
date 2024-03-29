package xyz.kazuthecat.coffeebot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.kazuthecat.coffeebot.ExposedDBHandler

class CoffeeLog (dbHandler: ExposedDBHandler) : Command() {
  // coffeelog is used with Exposed to create/handle/query the database
  // when creating the table the name of the class will be used, which is why it's all lower-case
  private val initializedUsers = mutableMapOf<Long, Int>()
  object coffeelog : Table() {
    val id = long("id").primaryKey()
    val cups = integer("cups")
  }

  init {
    name = "coffeelog"
    help = "logs a cup of coffee"
    aliases = arrayOf("cl")
    guildOnly = false

    dbHandler.getDBConnection()
    transaction {
      SchemaUtils.create(coffeelog)
      coffeelog.selectAll().forEach { initializedUsers[it[coffeelog.id]] = it[coffeelog.cups] }
    }
  }

  override fun execute(event: CommandEvent?) {
    if (event == null) { return }

    val args = event.args.split(" ")
    val author = event.author
    val reply : String
    reply = when (args[0]) {
      "add" -> addCups(args, author)
      "check" -> checkCups(author)
      else -> "I'm not sure what you want me to do ${author.asMention}"
    }
    event.reply(reply)
  }

  private fun addCups(args : List<String>, user : User) : String {
    val uid = user.idLong
    val oldCups = initializedUsers[user.idLong]

    var maxCups = false // Did user try to increase by more than 10?
    var newCups = 1     // How many cups does the user want to add?
    if (args.size > 1 && args[1].matches(Regex("\\d+"))) {
      try {
        newCups = args[1].toInt()
        if (newCups > 10) { newCups = 10; maxCups = true }
      } catch (_ : Exception) {}
    }

    transaction {
      if (oldCups == null) {
        // Initialize user if they're not in the database yet.
        coffeelog.insert { it[id] = uid; it[cups] = newCups }
        initializedUsers[uid] = newCups
      } else {
        // Update old entry if such an entry exists
        initializedUsers[uid] = oldCups + newCups
        coffeelog.update({coffeelog.id eq uid}) {
          it[cups] = oldCups + newCups
        }
      }
    }

    return if (!maxCups) {
      "Added $newCups to your cup count, ${user.asMention}. You're now at ${initializedUsers[uid]}"
    } else {
      "You're only allowed to add 10 cups at a time, so I added 10 to your cup count, ${user.asMention}. " +
        "You're now at ${initializedUsers[uid]}"
    }
  }

  private fun checkCups(user : User) : String {
    val currentCups = initializedUsers[user.idLong]
    return if (currentCups == null) {
      "${user.asMention} You've had 0 cups of coffee to date!"
    } else {
      "${user.asMention} You've had $currentCups cups of coffee to date!"
    }
  }
}
