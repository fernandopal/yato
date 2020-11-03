package es.fernandopal.yato.listeners;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandManager;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.files.Config;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class CommandListener extends ListenerAdapter {
    private final CommandManager manager = new CommandManager();
    private final Config config = new Config();

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        User user = event.getAuthor();
        if(user.isBot() || event.getMessage().isWebhookMessage()) { return; }

        if(Main.maintenance && !config.getList("bot-owners").contains(user.getId())) {
            event.getChannel().sendMessage(Emoji.ERROR + " The bot is on maintenance, please understand that it will not work for a few minutes.").queue();
            return;
        }

        final long guildId = event.getGuild().getIdLong();
        String prefix = Main.getDb().getPrefix(guildId);
        String raw = event.getMessage().getContentRaw();

        if(raw.startsWith(prefix)) { manager.handle(event, prefix); }
    }
}
