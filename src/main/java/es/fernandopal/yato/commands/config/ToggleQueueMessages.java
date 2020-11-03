package es.fernandopal.yato.commands.config;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.ydba.StaticHolder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class ToggleQueueMessages implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final Member member = ctx.getMember();
        final Guild guild = ctx.getGuild();

        if(!member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessage("You don't have permissions to use this command").queue();
            return;
        }

        Main.getDb().toggleQueueMessages(guild.getIdLong());

        if(Main.getDb().hasQueueMessagesEnabled(guild.getIdLong())) {
            channel.sendMessageFormat("The bot will now send a message every time an user adds a song/playlist to the queue.").queue();
        } else {
            channel.sendMessageFormat("The bot will not send 'added to the queue' messages anymore.").queue();
        }
    }

    @Override
    public String name() {
        return "queuemsgs";
    }
    @Override
    public String usage() {
        return "{prefix}:queuemsgs <prefix>";
    }
    @Override
    public String description() {
        return "Toggle the 'added to the queue' messages on/off (default: on)";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_ADMIN;
    }
    @Override
    public CommandType commandType() {
        return CommandType.CONFIG;
    }
    @Override
    public List<String> getAliases() {
        return List.of("togglequeuemsgs", "tqm");
    }
}
