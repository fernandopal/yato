package es.fernandopal.yato.commands.music;

import com.google.api.services.youtube.model.SearchResult;
import com.jagrosh.jdautilities.menu.ButtonMenu;
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
import es.fernandopal.yato.core.audio.YouTubeSearchHandler;
import es.fernandopal.yato.core.audio.sources.spotify.SpotifyConverter;
import es.fernandopal.yato.core.exceptions.CantJoinVoiceChannelException;
import es.fernandopal.yato.files.Config;
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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayCommand implements ICommand {
    private final MessageUtil msg = Main.getBotManager().getMsgu();
    private GuildAudioManager manager;
    private TextChannel musicChannel;

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();
        final Member member = ctx.getMember();
        final List<String> args = ctx.getArgs();

        final DatabaseManager db = Main.getDb();
        final SpotifyConverter spotifyConverter = new SpotifyConverter();

        musicChannel = Main.getTextChannelById(db.getMusicChannel(guild.getIdLong()));
        manager = AudioManagerController.getGuildAudioManager(guild);


        if(musicChannel == null) { musicChannel = tc; }

        if(manager.getLink().getState() != Link.State.CONNECTED && member.getVoiceState() != null) {
            try {
                manager.openConnection(member.getVoiceState().getChannel(), tc);
            } catch (CantJoinVoiceChannelException ignored) {
                return;

            }

        }

        if(args.size() < 1) {
            if(manager.getPlayer().isPaused()) {
                manager.getPlayer().setPaused(false);
                msg.send(musicChannel, Emoji.PLAY + " The player is no longer paused.", Color.WHITE);
            } else {
                manager.getPlayer().setPaused(true);
                msg.send(musicChannel, Emoji.PAUSE + " The player has been paused. To play a song use `y:p <song>`", Color.WHITE);

            }

        } else {
            if(args.get(0).equalsIgnoreCase("songoftheday") || args.get(0).equalsIgnoreCase("sotd")) {
                Thread task = new Thread(() -> manager.loadAndPlay(db.getSongOfTheDay().getUri(), ctx, musicChannel));
                task.start();

            } else if(args.get(0).equalsIgnoreCase("random")) {
                msg.sendWarn(musicChannel, Emoji.WARNING + " This command has been moved to `y:random <amount>`");

            } else {
                String searchTerm = args.get(0);
                if(searchTerm.startsWith("https://") || searchTerm.startsWith("http://")) {
                    final String fSearchTerm = searchTerm;

                    new Thread(() -> {

                       /* if(fSearchTerm.contains("spotify.com/track/")) {
                            final String songViaUrl = spotifyConverter.getSongViaUrl(fSearchTerm);

                            if(songViaUrl != null) {
                                manager.loadAndPlay(songViaUrl, ctx, musicChannel);

                            } else {
                                msg.sendError(musicChannel, "I can't load that song for some reason, please try again later or if the problem persist get support [here]("+db.getBotServerInvite()+")");

                            }

                        } else if(fSearchTerm.contains("spotify.com/playlist/")) {
                            final ArrayList<String> songsViaPlaylistUrl = spotifyConverter.getSongsViaPlaylistUrl(fSearchTerm);

                            if(songsViaPlaylistUrl != null && !songsViaPlaylistUrl.isEmpty()) {
                                if(Main.getDb().hasQueueMessagesEnabled(guild.getIdLong())) {
                                    msg.sendOk(musicChannel, "Adding `" + songsViaPlaylistUrl.size() + "` tracks to queue from a spotify playlist (this can take a bit so please give me a sec)");

                                }
                                songsViaPlaylistUrl.forEach(s -> manager.optimisedLoad(s, ctx, musicChannel));

                            } else {
                                msg.sendError(musicChannel, "I can't load that playlist for some reason, please try again later or if the problem persist get support [here]("+db.getBotServerInvite()+")");

                            }

                        } else if(fSearchTerm.contains("spotify.com/album/")) {
                            final ArrayList<String> getSongsViaAlbumUrl = spotifyConverter.getSongsViaAlbumUrl(fSearchTerm);

                            if(getSongsViaAlbumUrl != null && !getSongsViaAlbumUrl.isEmpty()) {
                                if(Main.getDb().hasQueueMessagesEnabled(guild.getIdLong())) {
                                    msg.sendOk(musicChannel, "Adding `" + getSongsViaAlbumUrl.size() + "` tracks to queue from a spotify album (this can take a bit so please give me a sec)");

                                }
                                getSongsViaAlbumUrl.forEach(s -> manager.optimisedLoad(s, ctx, musicChannel));

                            } else {
                                msg.sendError(musicChannel, "I can't load that album for some reason, please try again later or if the problem persist get support [here]("+db.getBotServerInvite()+")");

                            }

                        } else {*/
                            manager.loadAndPlay(fSearchTerm, ctx, musicChannel);

                        /*}*/

                    }).start();
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for (String str: args) { sb.append(str).append(" "); }
                searchTerm = sb.toString();

                List<SearchResult> results = YouTubeSearchHandler.search(searchTerm, 5L);

                if(results == null || results.isEmpty()) {
                    msg.sendError(musicChannel, Emoji.SEARCH + " I can't find a song with that search");
                    return;

                }

                if(Main.getDb().hasSearchResultsEnabled(guild.getIdLong())) {
                    StringBuilder searchResults = new StringBuilder();
                    searchResults.append("**Choose a result using the reactions:**").append("\n");

                    String[] choices = new String[results.size()];
                    for (int i = 0; i < results.size(); i++) {
                        final SearchResult searchResult = results.get(i);
                        final String emoji = Emoji.NUMBER(i);

                        choices[i] = emoji;
                        searchResults.append(emoji).append("`").append(formatTitle(searchResult.getSnippet().getTitle())).append("`").append("\n");

                    }

                    final ButtonMenu searchMenu = new ButtonMenu.Builder()
                            .setDescription(searchResults.toString())
                            .setColor(Color.ORANGE)
                            .setChoices(choices)
                            .addChoice(Emoji.CROSS)
                            .setEventWaiter(Main.getBotManager().getEventWaiter())
                            .setAction(re -> {
                                final String name = re.getName();
                                for (int i = 0; i < choices.length; i++) {
                                    if (name.equals(choices[i])) {
                                        String trackId = "https://www.youtube.com/watch?v=" + results.get(i).getId().getVideoId();
                                        manager.loadAndPlay(trackId, ctx, musicChannel);

                                    }

                                }
                            })
                            .setFinalAction(m -> {
                                try {
//                                message.delete().queue();
                                    m.delete().queue();

                                } catch (Exception ignored) { }

                            })
                            .build();

                    searchMenu.display(musicChannel);
                } else {
                    String trackId = "https://www.youtube.com/watch?v=" + results.get(0).getId().getVideoId();
                    manager.loadAndPlay(trackId, ctx, musicChannel);

                }

            }

        }

    }

    private @NotNull String formatTitle(@NotNull String title) {
        String newTitle = title.replace("&quot;", "\"");

        if (newTitle.length() > 50) {
            newTitle = newTitle.substring(0, 50) + " ...";

        }
        return newTitle;

    }

    @Override
    public String name() {
        return "play";

    }
    @Override
    public String usage() {
        return "{prefix}:play <sotd|url|search>";

    }
    @Override
    public String description() {
        return "Resume the player, add a song to the queue or play the song of the day";

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
        return List.of("p");

    }
}
