package es.fernandopal.yato.commands.music;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.audio.GuildAudioManager;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class ClearCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();

        final MessageUtil msg = Main.getBotManager().getMsgu();
        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        manager.getScheduler().clearQueue();
        msg.sendOk(musicChannel, Emoji.PAPER + " Queue cleared.");
    }

    @Override public String name() {
        return "clear";
    }
    @Override public String usage() {
        return "{prefix}:clear";
    }
    @Override public String description() {
        return "Clear the queue";
    }
    @Override public PermLevel permLevel() {
        return PermLevel.GUILD_DJ;
    }
    @Override public CommandType commandType() {
        return CommandType.MUSIC;
    }
    @Override public List<String> getAliases() { return List.of("cq", "clearqueue"); }
}
