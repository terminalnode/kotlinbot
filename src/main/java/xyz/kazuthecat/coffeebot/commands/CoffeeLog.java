package xyz.kazuthecat.coffeebot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.User;
import xyz.kazuthecat.coffeebot.DBHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoffeeLog extends Command {
    private final DBHandler dbHandler;
    private final List<User> initalizedUsers;

    public CoffeeLog(DBHandler dbHandler) {
        this.name = "coffeelog";
        this.help = "logs a cup of coffee";
        this.guildOnly = false;
        this.dbHandler = dbHandler;
        this.initalizedUsers = new ArrayList<>();

        String coffeelog_tbl = "CREATE TABLE IF NOT EXISTS coffeelog(" +
                "id      BIGINT NOT NULL, " +
                "cups    INT NOT NULL, " +
                "PRIMARY KEY (id) );";

        String coffeetypes_tbl = "CREATE TABLE IF NOT EXISTS coffeetypes(" +
                "id      BIGINT NOT NULL, " +
                "name    VARCHAR(30) NOT NULL, " +
                "cups    INT NOT NULL, " +
                "PRIMARY KEY (id, name) );";

        dbHandler.execute(new String[]{coffeelog_tbl, coffeetypes_tbl});
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] arglist = event.getArgs().split(" ");

        User user = event.getAuthor();
        String uid = user.getId();
        if (!initalizedUsers.contains(user)) {
            dbHandler.execute(
                    "INSERT IGNORE INTO coffeelog (id,cups) VALUES (?, 0);",
                    new String[][]{{uid}}
                    );
            initalizedUsers.add(user);
        }

        String reply;
        switch (arglist[0]) {
            case "add":   reply = addCups(arglist, uid); break;
            case "check": reply = checkCups(uid); break;
            default:      reply = "That's not a valid subcommand."; break;
        }
        event.reply(reply);
    }

    private String addCups(String[] arglist, String uid) {
        String numCups = "1";
        boolean cupOverflow = false;
        if (arglist.length > 1 && arglist[1].matches("\\d+")) {
            numCups = arglist[1];
            if (Integer.parseInt(numCups) > 10) {
                numCups = "10"; // Max number to avoid the cup number overflowing.
                cupOverflow = true;
            }
        }
        boolean result = dbHandler.execute(
                "UPDATE coffeelog SET cups = cups + ? WHERE id = ?;",
                new String[][]{{numCups, uid}}
                );

        if (result && cupOverflow) {
            return String.format("Success! I have added %s to your cup count! This is the maximum number of cups you can add at one time. :coffee:", numCups);
        } else if (result) {
            return String.format("Success! I have added %s to your cup count! :coffee:", numCups);
        } else {
            return "An error occurred, your cup count probably hasn't been updated. But who knows? :shrug:";
        }
    }

    private String checkCups(String uid) {
        String reply;
        List<Map<String, String>> resultSet = dbHandler.query("SELECT * FROM coffeelog WHERE id = ?", new String[]{uid});
        if (resultSet != null && resultSet.size() != 0) {
            String numCups = resultSet.get(0).get("cups");
            reply = String.format("You've had %s cups of coffee to date! :coffee:", numCups);
        } else {
            reply = "Something went wrong when trying to retrieve the number of cups. :shrug:";
        }
        return reply;
    }
}
