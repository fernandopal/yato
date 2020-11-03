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

import java.awt.*;

public class PauseCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();

        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        final MessageUtil msg = Main.getBotManager().getMsgu();
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        if(manager.getPlayer().isPaused()) {
            msg.send(musicChannel, Emoji.PAUSE + " The player is paused, use `y:p` to resume it.", Color.WHITE);
        } else {
            msg.send(musicChannel, Emoji.PAUSE + " The player has been paused.", Color.WHITE);
            manager.getPlayer().setPaused(true);
        }
    }

    @Override
    public String name() {
        return "pause";
    }
    @Override
    public String usage() {
        return "{prefix}:pause";
    }
    @Override
    public String description() {
        return "Pause the player";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override
    public CommandType commandType() {
        return CommandType.MUSIC;
    }
}
