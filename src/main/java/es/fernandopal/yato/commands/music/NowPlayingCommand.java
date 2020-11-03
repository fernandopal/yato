package es.fernandopal.yato.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.audio.GuildAudioManager;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class NowPlayingCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();
        final Member member = ctx.getMember();
        final User user = ctx.getAuthor();

        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        final MessageUtil msg = Main.getBotManager().getMsgu();
        final AudioTrack track = manager.getPlayer().getPlayingTrack();
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        if(track != null) {
            String[] uri = track.getInfo().uri.split("=");
            String imageUrl = (uri.length > 1) ? "https://img.youtube.com/vi/" + uri[1] + "/1.jpg" : "https://i.imgur.com/bCNQlm6.jpg";

            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor("Now Playing")
                    .setTitle(track.getInfo().title, track.getInfo().uri)
                    .setThumbnail(imageUrl)
                    .addField("Duration", Main.getBotManager().getTimestamp(manager.getPlayer().getTrackPosition()) + "/" + Main.getBotManager().getTimestamp(track.getDuration()), true)
                    .addField("Channel", track.getInfo().author, true)
                    .setFooter("Requested by " + member.getEffectiveName(), user.getEffectiveAvatarUrl());
            musicChannel.sendMessage(embed.build()).queue();
        } else {
            msg.sendError(musicChannel, Emoji.ERROR + " There isn't a track currently playing.");
        }
    }

    @Override
    public String name() {
        return "nowplaying";
    }
    @Override
    public String usage() {
        return "{prefix}:np";
    }
    @Override
    public String description() {
        return "Shows info about the current song";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override
    public CommandType commandType() {
        return CommandType.MUSIC;
    }
    @Override
    public List<String> getAliases() {
        return List.of("np", "current", "playing", "song");
    }
}
