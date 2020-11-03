package es.fernandopal.yato.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.audio.GuildAudioManager;
import es.fernandopal.yato.core.exceptions.CantJoinVoiceChannelException;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import es.fernandopal.ydba.database.DatabaseManager;
import lavalink.client.io.LavalinkSocket;
import lavalink.client.io.Link;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class RadioCommand implements ICommand {
    private final MessageUtil msg = Main.getBotManager().getMsgu();

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();
        final Member member = ctx.getMember();
        final List<String> args = ctx.getArgs();

        final DatabaseManager db = Main.getDb();

        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        if(manager.getLink().getState() != Link.State.CONNECTED && member.getVoiceState() != null) {
            try {
                manager.openConnection(member.getVoiceState().getChannel(), tc);

            } catch (CantJoinVoiceChannelException ignored) {
                return;

            }

        }

        //TODO: Check why playback is stopping after a few seconds playing

        if(args.size() < 1) {
            playRadio(musicChannel, true, manager, ctx);

        } else {
            if(args.get(0).equalsIgnoreCase("kpop")) {
                playRadio(musicChannel, true, manager, ctx);

            } else if(args.get(0).equalsIgnoreCase("jpop")) {
                playRadio(musicChannel, false, manager, ctx);

            } else {
                msg.sendOk(musicChannel, Emoji.RADIO + " The correct usage for this command is " +  usage().replace("{prefix}", db.getPrefix(guild.getIdLong())));

            }

        }

    }

    private void playRadio(TextChannel musicChannel, boolean kpop, GuildAudioManager manager, CommandContext ctx) {
        if(kpop) {
            playRadioUrl(manager, "https://listen.moe/kpop/stream", ctx);
            msg.sendOk(musicChannel, Emoji.RADIO + " You are now listening to the k-pop radio provided by [listen.moe](https://listen.moe)");

        } else {
            playRadioUrl(manager, "https://listen.moe/stream", ctx);
            msg.sendOk(musicChannel, Emoji.RADIO + " You are now listening to the j-pop radio provided by [listen.moe](https://listen.moe)");

        }

    }

    public void playRadioUrl(GuildAudioManager manager,  String trackUrl, CommandContext ctx) {
        AudioManagerController.getPlayerManager().loadItemOrdered(this, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                manager.getPlayer().playTrack(track);
            }

            @Override public void playlistLoaded(AudioPlaylist playlist) { }
            @Override public void loadFailed(FriendlyException e) { }
            @Override public void noMatches() { }

        });

    }

    @Override public String name() {
        return "radio";
    }
    @Override public String usage() {
        return "{prefix}:radio <kpop|jpop>";
    }
    @Override public String description() {
        return "Makes the bot start streaming listen.moe radio";
    }
    @Override public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override public CommandType commandType() {
        return CommandType.MUSIC;
    }
    @Override public List<String> getAliases() { return List.of("listen.moe"); }
}
