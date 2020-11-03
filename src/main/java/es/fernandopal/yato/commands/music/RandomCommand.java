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
import es.fernandopal.ydba.entities.TrackInfo;
import lavalink.client.io.LavalinkSocket;
import lavalink.client.io.Link;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

public class RandomCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomCommand.class);
    private GuildAudioManager manager;
    private TextChannel musicChannel;
    private MessageUtil msg;

    @Override
    public void handle(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();
        final Member member = ctx.getMember();
        final List<String> args = ctx.getArgs();

        final DatabaseManager db = Main.getDb();

        musicChannel = Main.getTextChannelById(db.getMusicChannel(guild.getIdLong()));
        manager = AudioManagerController.getGuildAudioManager(guild);
        msg = Main.getBotManager().getMsgu();

        if(musicChannel == null) { musicChannel = tc; }

        if(manager.getLink().getState() != Link.State.CONNECTED && member.getVoiceState() != null) {
            try {
                manager.openConnection(member.getVoiceState().getChannel(), tc);
            } catch (CantJoinVoiceChannelException ex) {
//                ex.printStackTrace();
                return;
            }
        }
        if(args.size() < 1) {
            final TrackInfo randomSong = db.getRandomSong();
            if (randomSong == null || randomSong.getUri() == null) {
                msg.sendError(musicChannel, "We don't have enough data yet on our db");
            } else {
                loadAndPlay(manager, randomSong.getUri(), ctx);
            }
        } else {
            try {
                int nSongs = Integer.parseInt(args.get(0));
//                int mSongs = 15;
                int mSongs = 100;

//                Rank r = new RankManager().getRank(user);

//                if(r.equals(Rank.CHONK)) {
//                    mSongs = 100;
//                } else if(r.equals(Rank.YATO_LOVER)) {
//                    mSongs = 50;
//                } else if(r.equals(Rank.SUPPORTER)) {
//                    mSongs = 30;
//                }

                if(nSongs > mSongs || (manager.getScheduler().queue.size() + nSongs) > mSongs) {
                    msg.sendError(musicChannel, "You have " + manager.getScheduler().queue.size() + " songs in queue, if I add " + nSongs + " the queue will exceed the limit of " + mSongs + " songs for your rank, please insert less songs or donate to extend your capacity at our patreon: https://patreon.com/yatods");
                } else {

                    new Thread(() -> {
                        if(Main.getDb().hasQueueMessagesEnabled(guild.getIdLong())) {
                            msg.sendOk(musicChannel, "Adding `" + nSongs + "` tracks to queue from our database of " + db.getDatabaseSize() + " songs (this can take a bit so please give me a sec)");
                        }
                        for (int i = 0; i < nSongs; i++) {
                            final TrackInfo randomSong = db.getRandomSong();
                            if (randomSong == null || randomSong.getUri() == null) {
                                msg.sendError(musicChannel, "We don't have enough data yet on our db");
                                break;
                            } else {
                                optimisedLoad(randomSong.getUri(), ctx.getEvent(), ctx);
                            }
                        }
                    }).start();
                }
            } catch(NumberFormatException ignored) { }
        }
    }

    private void optimisedLoad(@NotNull String searchTerm, GuildMessageReceivedEvent e, @NotNull CommandContext ctx) {
        final User user = ctx.getAuthor();

        final String trackUrl = searchTerm.startsWith("<") && searchTerm.endsWith(">") ? searchTerm.substring(1, searchTerm.length() - 1) : searchTerm;

        AudioManagerController.getPlayerManager().loadItem(trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                String[] time = Main.getBotManager().getTimestamp(track.getDuration()).split(":");
                if((Integer.parseInt(time[0]) >= 3 && time.length == 3) || time.length > 3) { return; }

                track.setUserData(e);
                manager.getScheduler().queue(track);
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

                    track.setUserData(e);
                    manager.getScheduler().queue(track);
                }
            }

            @Override public void noMatches() { }
            @Override public void loadFailed(FriendlyException e) { LOGGER.warn("Track '" + trackUrl + "' can't be loaded on this node."); }

        });
    }

    private void loadAndPlay(GuildAudioManager manager, @NotNull String searchTerm, @NotNull CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final User user = ctx.getAuthor();

        final String trackUrl = searchTerm.startsWith("<") && searchTerm.endsWith(">") ? searchTerm.substring(1, searchTerm.length() - 1) : searchTerm;
        LOGGER.info("Loaded track '" + trackUrl + "' requested by '" + user + "(" + user.getId() + ")' at '" + guild.getName() + "'" );

        AudioManagerController.getPlayerManager().loadItemOrdered(manager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String[] time = Main.getBotManager().getTimestamp(track.getDuration()).split(":");
                if((Integer.parseInt(time[0]) >= 3 && time.length == 3) || time.length > 3) {
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
                manager.getScheduler().queue(track);
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
                    manager.getScheduler().queue(track);
                }
            }

            @Override public void noMatches() { msg.sendError(musicChannel, "I can't find any songs with that search."); }

            @Override
            public void loadFailed(FriendlyException e) {
                LOGGER.warn("Track '" + trackUrl + "' can't be loaded on this node.");
                msg.sendError(musicChannel, "Error when trying to load the song. The most common cause for this error is trying to play age-restricted/region-restricted content.\nIf you think this can be due to an error please ask for support here: https://discord.gg/TSHGVQt");
            }
        });
    }

    @Override public String name() {
        return "random";
    }
    @Override public String usage() {
        return "{prefix}:play <sotd|random|url|search>";
    }
    @Override public String description() {
        return "Play a random song";
    }
    @Override public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override public CommandType commandType() {
        return CommandType.MUSIC;
    }
    @Override public List<String> getAliases() {
        return List.of("randomplay", "rplay", "rsongs", "playrandom", "prandom", "randomsongs");
    }
}
