package xyz.kazuthecat.coffeebot.listeners;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import xyz.kazuthecat.coffeebot.settings.SettingsAbstract;

import java.util.concurrent.TimeUnit;

/**
 * Example of a simple listener that will reply when confronted with any message
 * that contains both a mention of the bot and the word "hello" or "hi".
 * It demonstrates the listener functionality as well as waiter.waitForEvent().
 *
 * This class lends heavily from John Grosh's example command HelloCommand:
 * https://github.com/jagrosh/ExampleBot/blob/master/src/main/java/com/jagrosh/examplebot/commands/HelloCommand.java
 *
 * @author Alexander Rundberg
 */
public class HelloListener extends ListenerAdapter {
  private final EventWaiter waiter;
  private final SettingsAbstract settings;
  private final String questionSetting, responseSetting;

  public HelloListener(EventWaiter waiter, SettingsAbstract settings) {
    this.waiter = waiter;
    this.settings = settings;

    this.questionSetting = "hello.question";
    settings.setDefaults(questionSetting, "{user} Hi! What's your name?", false, true);
    this.responseSetting = "hello.response";
    settings.setDefaults(responseSetting, "Hello {content}, I'm {botname}.", false, true);
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    Message message = event.getMessage();
    String content = message.getContentRaw().toLowerCase();
    User bot = message.getJDA().getSelfUser();
    User user = event.getAuthor();

    // If "hi" or "hello" wasn't in the message, or if the bot wasn't mentioned: abort.
    boolean botMentioned = message.getMentionedUsers().contains(bot);
    boolean saidHello = content.matches(".*\\bhello\\b.*") || content.matches(".*\\bhi\\b.*");
    if (!botMentioned || !saidHello || bot.equals(user)) return;

    MessageChannel channel = event.getChannel();
    String questionMsg = settings.getSetting(questionSetting, message);
    channel.sendMessage(questionMsg).queue();

    // Note that the waiter is not blocking. You can't stage multiple waiters after one another.
    // TODO Waiter isn't working properly in Kotlin port yet
    waiter.waitForEvent(
      // First parameter is a class, if the event is not of this class it will be ignored.
      MessageReceivedEvent.class,

      // Second parameter is a predicate, if this is true the event will trigger.
      e -> e.getAuthor().equals(user)    // Check that author is the same.
        &&   e.getChannel().equals(channel)  // Check that channel is the same.
        &&  !e.getMessage().equals(message), // Check that we're not looking at the same msg.

      // Third parameter is some kind of event that extends consumer.
      e -> channel.sendMessage(settings.getSetting(responseSetting, e.getMessage())).queue(),

      // Fourth parameter is an optional time out followed by an optional event extending consumer.
      20, TimeUnit.SECONDS,
      () -> channel.sendMessage(user.getAsMention() + " TOO SLOW!").queue()
    );
  }
}
