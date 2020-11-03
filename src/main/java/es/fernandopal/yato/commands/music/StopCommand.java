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
import lavalink.client.io.Link;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

public class StopCommand implements ICommand {

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();

        final MessageUtil msg = Main.getBotManager().getMsgu();
        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        if (manager.getLink().getState() == Link.State.NOT_CONNECTED) { manager.closeConnection(); }
        manager.destroy();
        msg.sendOk(musicChannel, Emoji.STOP + " Player stopped.");
    }

    @Override
    public String name() {
        return "stop";
    }
    @Override
    public String usage() {
        return "{prefix}:stop";
    }
    @Override
    public String description() {
        return "Clear the queue and stop the player";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_DJ;
    }
    @Override
    public CommandType commandType() {
        return CommandType.MUSIC;
    }
}
