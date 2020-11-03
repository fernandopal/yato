package es.fernandopal.yato.commands.music;

import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.jagrosh.jdautilities.menu.Paginator;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.audio.GuildAudioManager;
import es.fernandopal.yato.core.audio.TrackScheduler;
import es.fernandopal.yato.core.audio.playlists.PlaylistsUtils;
import es.fernandopal.yato.core.scheduler.ScheduleHandler;
import es.fernandopal.yato.core.scheduler.tasks.SelectedPlaylistTimeoutJob;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import es.fernandopal.ydba.database.DatabaseManager;
import es.fernandopal.ydba.entities.YPlaylist;
import es.fernandopal.ydba.entities.YTrack;
import es.fernandopal.ydba.entities.YUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaylistCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistCommand.class);

    private ScheduledFuture timeout;
    private final String YT_VIDEO_REGEX = "^.*(youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=|\\&v=)([^#\\&\\?]*).*";
    private final String YT_PLIST_REGEX = "[?&]list=([^#\\&\\?]+)";

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();;
        final List<String> args = ctx.getArgs();
        final User author = ctx.getAuthor();

        final MessageUtil msg = Main.getBotManager().getMsgu();
        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if (musicChannel == null) { musicChannel = tc; }

        if ((args.size() < 1) || (args.size() >= 1 && args.get(0).equalsIgnoreCase("help"))) {//TODO: plist help
            PrivateChannel pc = author.openPrivateChannel().complete();

            final String dj = Emoji.S_ORANGE_DIAMOND;
            final String usr = Emoji.S_BLUE_DIAMOND;

            String csb = Emoji.CLIPBOARD + " __**PLAYLIST COMMANDS**__ " + Emoji.CLIPBOARD + " \n" +
                    "**Permission levels:**\n" +
                    Emoji.S_ORANGE_DIAMOND + " - Commands only for server DJ's\n" +
                    Emoji.S_BLUE_DIAMOND + " - Commands for everyone\n" +
                    "\n" +
                    usr + "  **" + "plist help" + "** - `" + "Show this help menu" + "`\n" +
                    usr + "  **" + "plist create <name>" + "** - `" + "Create a new playlist" + "`\n" +
                    usr + "  **" + "plist select <name>" + "** - `" + "Select a playlist" + "`\n" +
                    usr + "  **" + "plist delete" + "** - `" + "Delete the selected playlist" + "`\n" +
                    usr + "  **" + "plist add <url>" + "** - `" + "Add a song to the selected playlist" + "`\n" +
                    usr + "  **" + "plist add" + "** - `" + "Add the song that is currently playing to the selected playlist" + "`\n" +
                    usr + "  **" + "plist remove <song id>" + "** - `" + "Remove the song from the selected playlist" + "`\n" +
                    usr + "  **" + "plist clear" + "** - `" + "Clear the selected playlist" + "`\n" +
                    usr + "  **" + "plist rename <new name>" + "** - `" + "Rename the selected playlist" + "`\n" +
                    usr + "  **" + "plist list" + "** - `" + "Show a list of your playlists" + "`\n" +
                    usr + "  **" + "plist list <playlist name>" + "** - `" + "Show a list of the tracks in a playlist" + "`\n" +
                    usr + "  **" + "plist randomfill <amount>" + "** - `" + "Add X random songs to the selected playlist" + "`\n" +
                    usr + "  **" + "plist randomplay <playlist name>" + "** - `" + "Play a playlist in a random order" + "`\n" +
                    usr + "  **" + "plist randomplay" + "** - `" + "Play the selected playlist in a random order" + "`\n" +
                    usr + "  **" + "plist play <playlist name>" + "** - `" + "Play a playlist" + "`\n" +
                    usr + "  **" + "plist play" + "** - `" + "Play the selected playlist" + "`\n" +
                    dj + "  **" + "plist forceplay <playlist name>" + "** - `" + "Force to play a playlist (clears the queue)" + "`\n" +
                    dj + "  **" + "plist forceplay" + "** - `" + "Force to play the selected playlist (clears the queue)" + "`\n";
            msg.sendPm(pc, csb, Color.ORANGE);
            msg.sendOk(musicChannel, author.getAsMention() + " I've sent you the playlists help to your dm");
        } else if (args.size() >= 1) {
            if (args.get(0).equalsIgnoreCase("create")) {//TODO: plist create
                if (args.size() < 2) {
                    msg.sendError(musicChannel, Emoji.ERROR + " Please insert a name for the playlist `plist create <name>`");
                    return;
                }

                final String playlist_name = args.get(1);

                YPlaylist yPlaylist = new YPlaylist(playlist_name, author.getIdLong(), true);
                if (Main.getDb().createPlaylist(yPlaylist)) {
                    msg.sendOk(musicChannel, "Playlist `" + playlist_name + "` successful created.");
                } else {
                    msg.sendError(musicChannel, Emoji.ERROR + " You already have a playlist with that name.");
                }

            } else if (args.get(0).equalsIgnoreCase("select")) {//TODO: plist select
                if (args.size() < 2) {
                    msg.sendError(musicChannel, Emoji.ERROR + " Please insert a name for the playlist `plist select <name>`");
                    return;
                }

                final String playlist_name = args.get(1);
                final YPlaylist yPlaylist = Main.getDb().getPlaylist(playlist_name, author.getIdLong());

                if (yPlaylist == null) {
                    msg.sendError(musicChannel, Emoji.ERROR + " I can't find a playlist with the name `" + playlist_name + "`, you can list your playlists with `plist list`");
                    return;
                }

                final YUser yUser = new YUser(author.getIdLong(), yPlaylist.getPlaylistId());
                if (!Main.getDb().insertUser(yUser)) {
                    if (!Main.getDb().selectPlaylist(yUser.getUserId(), yPlaylist.getPlaylistId())) {
                        msg.sendError(musicChannel, Emoji.ERROR + " There was an unknown error selecting that playlist, please contact support at [The support server](" + Main.getDb().getBotServerInvite() + ") or mail@fernandopal.es");
                        return;
                    }
                }

                if (timeout != null) {
                    timeout.cancel(true);
                    timeout = null;
                }

                timeout = ScheduleHandler.registerUniqueJob(new SelectedPlaylistTimeoutJob(yUser.getUserId(), yUser.getSelectedPlaylist()));
                msg.sendOk(musicChannel, "Playlist `" + playlist_name + "` successful selected, It will last for 15 minutes or until you select a new one.");

            } else if (args.get(0).equalsIgnoreCase("delete")) {//TODO: plist delete
                final String selectedPlaylistId = Main.getDb().getSelectedPlaylist(author.getIdLong());

                if (selectedPlaylistId == null) {
                    msg.sendError(musicChannel, Emoji.ERROR + " Please select a playlist first with `plist select <name>`");
                    return;
                }

                final YPlaylist selectedPlaylist = Main.getDb().getPlaylist(selectedPlaylistId);

                final TextChannel finalMusicChannel = musicChannel;
                final ButtonMenu searchMenu = new ButtonMenu.Builder()
                        .setDescription("Are you sure you want to delete the playlist `" + selectedPlaylist.getPlaylistName() + "`?")
                        .setColor(Color.RED)
                        .addChoice(Emoji.CHECK_2)
                        .addChoice(Emoji.CROSS)
                        .setEventWaiter(Main.getBotManager().getEventWaiter())
                        .setAction(re -> {
                            System.out.println(re.toString());

                            final String name = re.getName();
                            if (name.equals(Emoji.CHECK_2)) {
                                Main.getDb().deletePlaylist(selectedPlaylist);
                                Main.getDb().selectPlaylist(author.getIdLong(), null);
                                msg.sendOk(finalMusicChannel, Emoji.CHECK + " Playlist `" + selectedPlaylist.getPlaylistName() + "` has been deleted.");
                            }
                        })
                        .setFinalAction(m -> {
                            try {
                                m.delete().queue();
                            } catch (PermissionException ignore) {
                            }
                        })
                        .build();

                searchMenu.display(musicChannel);

            } else if (args.get(0).equalsIgnoreCase("add")) {//TODO: plist add <name>
                if (args.size() < 2) {
                    final AudioTrack playingTrack = manager.getPlayer().getPlayingTrack();

                    if (playingTrack == null) {
                        msg.sendError(musicChannel, Emoji.ERROR + " There are no tracks playing right now. Please write the url of the song to insert `plist add <url>`");
                        return;
                    } else {
                        final String selectedPlaylistId = Main.getDb().getSelectedPlaylist(author.getIdLong());

                        if (selectedPlaylistId == null) {
                            msg.sendError(musicChannel, Emoji.ERROR + " Please select a playlist first with `plist select <name>`");
                            return;
                        }

                        final YPlaylist selectedPlaylist = Main.getDb().getPlaylist(selectedPlaylistId);
                        if (selectedPlaylist == null) {
                            msg.sendError(musicChannel, Emoji.ERROR + " Internal error. Please select the playlist again and if this persist ask for support on the [discord server](" + Main.getDb().getBotServerInvite() + ")");
                            return;
                        }

                        final PlaylistsUtils playlistsUtils = new PlaylistsUtils();

                        YTrack yTrack = Main.getDb().getYTrack(playingTrack.getInfo().uri);
                        if (yTrack == null) {
                            try {
                                yTrack = new YTrack(playingTrack.getInfo().uri, playlistsUtils.encodeTrack(playingTrack), playingTrack.getInfo().title, 0, 0);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        assert yTrack != null;
                        if(selectedPlaylist.getPlaylistTracks().contains(yTrack.getTrackId())) {
                            msg.sendError(musicChannel, Emoji.ERROR + " That track is already on the playlist.");
                            return;
                        }

                        Main.getDb().addYTrackToYPlaylist(yTrack, selectedPlaylist);
                        msg.sendOk(musicChannel, Emoji.CHECK + " Song `" + playingTrack.getInfo().title + "` added to the playlist `" + selectedPlaylist.getPlaylistName() + "`");

                        return;
                    }
                }

                final String song_url = args.get(1);

                final Pattern VIDEO_PATERN = Pattern.compile(YT_VIDEO_REGEX);
                final Matcher VIDEO_MATCHER = VIDEO_PATERN.matcher(song_url);

                if (!VIDEO_MATCHER.matches()) {
                    msg.sendError(musicChannel, Emoji.ERROR + " That url is not valid, you should insert a valid youtube song url (playlists are not allowed)");
                    return;
                }

                final String selectedPlaylistId = Main.getDb().getSelectedPlaylist(author.getIdLong());

                if (selectedPlaylistId == null) {
                    msg.sendError(musicChannel, Emoji.ERROR + " Please select a playlist first with `plist select <name>`");
                    return;
                }

                final YPlaylist selectedPlaylist = Main.getDb().getPlaylist(selectedPlaylistId);
                if (selectedPlaylist == null) {
                    msg.sendError(musicChannel, Emoji.ERROR + " Internal error. Please select the playlist again and if this persist ask for support on the [discord server](" + Main.getDb().getBotServerInvite() + ")");
                    return;
                }

                YTrack yTrack = Main.getDb().getYTrack(song_url);
                if (yTrack == null) {
                    yTrack = new YTrack(song_url, null, "unknown track", 0, 0);
                }

                if(selectedPlaylist.getPlaylistTracks().contains(yTrack.getTrackId())) {
                    msg.sendError(musicChannel, Emoji.ERROR + " That track is already on the playlist.");
                    return;
                }

                Main.getDb().addYTrackToYPlaylist(yTrack, selectedPlaylist);
                msg.sendOk(musicChannel, Emoji.CHECK + " Song `" + song_url + "` added to the playlist `" + selectedPlaylist.getPlaylistName() + "`");

            } else if (args.get(0).equalsIgnoreCase("list")) {//TODO: plist list <playlist name>
                if (args.size() < 2) {
                    final List<YPlaylist> yPlaylists = Main.getDb().getPlaylists(author.getIdLong());

                    if (yPlaylists.isEmpty()) {
                        msg.sendError(musicChannel, Emoji.ERROR + " You don't have ay playlists, create one with `plist create <name>`");
                        return;
                    }

                    final Paginator.Builder playlists = new Paginator.Builder().setColumns(1)
                            .setItemsPerPage(10)
                            .showPageNumbers(true)
                            .waitOnSinglePage(false)
                            .useNumberedItems(false)
                            .setColor(Color.ORANGE)
                            .setText("**" + author.getName() + " playlists:**")
                            .setUsers(author)
                            .setFinalAction(m -> {
                                try {
                                    m.clearReactions().queue();
                                } catch (PermissionException ex) {
                                    m.delete().queue();
                                }
                            })
                            .setEventWaiter(Main.getBotManager().getEventWaiter())
                            .setTimeout(1, TimeUnit.MINUTES);

                    int page = 1;
                    playlists.clearItems();
                    yPlaylists.stream().map(p -> "**" + p.getPlaylistName() + "** (" + p.getPlaylistTracks().size() + " tracks)").forEach(playlists::addItems);
                    playlists.build().paginate(musicChannel, page);
                    return;
                }

                final String playlist_name = args.get(1);
                final YPlaylist yPlaylist = Main.getDb().getPlaylist(playlist_name, author.getIdLong());

                if (yPlaylist == null) {
                    msg.sendError(musicChannel, Emoji.ERROR + " I can't find a playlist with the name `" + playlist_name + "`, you can list your playlists with `plist list`");
                    return;
                }

                final Paginator.Builder tracks = new Paginator.Builder().setColumns(1)
                        .setItemsPerPage(10)
                        .showPageNumbers(true)
                        .waitOnSinglePage(false)
                        .useNumberedItems(false)
                        .setColor(Color.ORANGE)
                        .setText("**" + yPlaylist.getPlaylistName() + " tracks:**")
                        .setUsers(author)
                        .setFinalAction(m -> {
                            try {
                                m.clearReactions().queue();
                            } catch (PermissionException ex) {
                                m.delete().queue();
                            }
                        })
                        .setEventWaiter(Main.getBotManager().getEventWaiter())
                        .setTimeout(2, TimeUnit.MINUTES);

                int page = 1;
                tracks.clearItems();

                ArrayList<YTrack> yPlistTracks = new ArrayList<>();
                yPlaylist.getPlaylistTracks().forEach(trackId -> yPlistTracks.add(Main.getDb().getYTrackById(trackId)));

                yPlistTracks.stream().map(track -> "**" + track.getTrackName() + "** | [" + track.getTrackUri() + "]").forEach(tracks::addItems);
                tracks.build().paginate(musicChannel, page);
            }
        }
    }

    @Override public String name() {
        return "playlist";
    }
    @Override public String usage() {
        return "{prefix}:playlist";
    }
    @Override public String description() {
        return "Manage playlists";
    }
    @Override public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override public CommandType commandType() {
        return CommandType.MUSIC;
    }
    @Override public List<String> getAliases() { return List.of("plist"); }
}
