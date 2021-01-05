package es.fernandopal.yato.core.audio;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.music.PlayCommand;
import es.fernandopal.yato.core.exceptions.CantJoinVoiceChannelException;
import es.fernandopal.yato.files.Config;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.IPlayerEventListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class GuildAudioManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildAudioManager.class);
    private final MessageUtil msg = Main.getBotManager().getMsgu();
    private final Config config = new Config();
    private final Guild guild;
    private final JdaLink link;
    private final LavalinkPlayer player;
    private final TrackScheduler scheduler;

    public GuildAudioManager(Guild guild) {
        this.guild = guild;
        this.link = Main.getBotManager().getLavalink().getLink(guild);
        this.player = this.link.getPlayer();
        this.scheduler = new TrackScheduler(guild, this.player);
        this.player.addListener(this.scheduler);
    }

    public void openConnection(VoiceChannel channel, TextChannel tc) throws CantJoinVoiceChannelException {
        try {
            this.link.connect(channel);
        } catch (Exception ex) {
            MessageUtil msg = Main.getBotManager().getMsgu();
            msg.sendError(tc, "I can't join a voice channel, check if I have permissions to join the channel you're in or if you are on not in one please join one.");
            throw new CantJoinVoiceChannelException(ex.getLocalizedMessage());
        }
    }

    public void destroy() {
        link.destroy();
        AudioManagerController.removeGuildAudioManager(guild);
    }

    public void loadAndPlay(@NotNull String searchTerm, @NotNull CommandContext ctx, TextChannel musicChannel) {
        final Guild guild = ctx.getGuild();
        final User user = ctx.getAuthor();

        final String trackUrl = searchTerm.startsWith("<") && searchTerm.endsWith(">") ? searchTerm.substring(1, searchTerm.length() - 1) : searchTerm;
        LOGGER.info("Loaded track '" + trackUrl + "' requested by '" + user + "(" + user.getId() + ")' at '" + guild.getName() + "'" );

        AudioManagerController.getPlayerManager().loadItemOrdered(this, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String[] time = Main.getBotManager().getTimestamp(track.getDuration()).split(":");
                if(((Integer.parseInt(time[0]) >= 3 && time.length == 3) || time.length > 3) && !config.getList("bot-owners").contains(user.getId())) {
                    msg.sendOk(musicChannel, "To add a song longer than 3h please donate at our patreon: https://patreon.com/yatods");
                    return;

                }

                if(Main.getDb().hasQueueMessagesEnabled(guild.getIdLong())) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.GREEN);
                    embed.setDescription(Emoji.CHECK + " Track `" + track.getInfo().title + "` added to the queue.");
                    embed.setFooter("Requested by " + user.getAsTag(), user.getAvatarUrl());
                    musicChannel.sendMessage(embed.build()).queue();

                }

                track.setUserData(ctx.getEvent());
                getScheduler().queue(track);

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(Main.getDb().hasQueueMessagesEnabled(guild.getIdLong())) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.GREEN);
                    embed.setDescription(Emoji.CHECK + " Adding `" + playlist.getTracks().size() + "` tracks to queue from playlist: `" + playlist.getName() + "`");
                    embed.setFooter("Requested by " + user.getAsTag(), user.getAvatarUrl());
                    musicChannel.sendMessage(embed.build()).queue();

                }

                List<AudioTrack> tracks = playlist.getTracks();
                for(AudioTrack track: tracks) {
                    String[] time = Main.getBotManager().getTimestamp(track.getDuration()).split(":");
                    if((Integer.parseInt(time[0]) >= 3 && time.length == 3)) {
                        msg.sendOk(musicChannel, "That playlist has a song longer than 3h, I'll not add it. To remove this limitation you can always donate at our patreon: https://patreon.com/yatods");

                    }

                    track.setUserData(ctx.getEvent());
                    getScheduler().queue(track);

                }

            }

            @Override public void noMatches() { msg.sendError(musicChannel, "I can't find any songs with that search."); }

            @Override
            public void loadFailed(FriendlyException e) {
                LOGGER.warn("Track '" + trackUrl + "' can't be loaded on this node.");
                msg.sendError(musicChannel, "Error when trying to load the song. The most common cause for this error is trying to play age-restricted/region-restricted content.\nIf you think this can be due to an error please ask for support here: https://discord.gg/TSHGVQt");

                warnIf429(e, ctx);
            }

        });

    }

    //This is garbage but it works idk
    public void optimisedLoad(@NotNull String searchTerm, @NotNull CommandContext ctx, TextChannel musicChannel) {
        final User user = ctx.getAuthor();

        final String trackUrl = searchTerm.startsWith("<") && searchTerm.endsWith(">") ? searchTerm.substring(1, searchTerm.length() - 1) : searchTerm;

        AudioManagerController.getPlayerManager().loadItem(trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                String[] time = Main.getBotManager().getTimestamp(track.getDuration()).split(":");
                if((Integer.parseInt(time[0]) >= 3 && time.length == 3) || time.length > 3) { return; }

                track.setUserData(ctx.getEvent());
                getScheduler().queue(track);

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(Main.getDb().hasQueueMessagesEnabled(ctx.getGuild().getIdLong())) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.GREEN);
                    embed.setDescription(Emoji.CHECK + " Adding `" + playlist.getTracks().size() + "` tracks to queue from playlist: `" + playlist.getName() + "`");
                    embed.setFooter("Requested by " + user.getAsTag(), user.getAvatarUrl());
                    musicChannel.sendMessage(embed.build()).queue();

                }

                List<AudioTrack> tracks = playlist.getTracks();
                for(AudioTrack track: tracks) {
                    String[] time = Main.getBotManager().getTimestamp(track.getDuration()).split(":");
                    if((Integer.parseInt(time[0]) >= 3 && time.length == 3) || time.length > 3) { return; }

                    track.setUserData(ctx.getEvent());
                    getScheduler().queue(track);

                }

            }

            @Override public void noMatches() { }
            @Override public void loadFailed(FriendlyException e) {
                LOGGER.warn("Track '" + trackUrl + "' can't be loaded on this node.");

                warnIf429(e, ctx);
            }

        });

    }

    private void warnIf429(Exception e, CommandContext ctx) { //TODO: fix in-discord logs
//        if(Arrays.toString(e.getStackTrace()).contains("429")) { //TODO: make mention all config.getList("bot-owners")
//            msg.sendWebhookMsg("429 code received, you need to perform a ip rotation " + ctx.getJDA().retrieveUserById("214829164253937674").complete().getAsMention(), "https://discordapp.com/api/webhooks/733019361601650880/V_tUSYJgeGR1MkESRQAZbRZStvpirj2pC5o8bXayho9shcPa2xMXQl8SQ-bgqVlfNLzW");
//        }
    }

    public void closeConnection() {
        link.disconnect();
    }
    public TrackScheduler getScheduler() {
        return scheduler;
    }
    public JdaLink getLink() {
        return link;
    }
    public LavalinkPlayer getPlayer() {
        return player;
    }
    public void resetPlayer(Guild guild) {
        link.resetPlayer();
    }

}
