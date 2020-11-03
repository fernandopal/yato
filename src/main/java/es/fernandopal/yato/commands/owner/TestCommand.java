package es.fernandopal.yato.commands.owner;

import com.google.api.services.youtube.model.SearchResult;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.core.BotManager;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.audio.GuildAudioManager;
import es.fernandopal.yato.core.audio.YouTubeSearchHandler;
import es.fernandopal.yato.core.audio.sources.spotify.SpotifyConverter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCommand.class);
    private final SpotifyApi spotifyApi = BotManager.getSpotifyApi();

//    private static final Pattern SPOTIFY_PATTERN = Pattern.compile("(?:[/:])(album|playlist|track)(?:[/:])([a-z0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    @Override
    public void handle(CommandContext ctx) {
        long startTime = System.nanoTime();

        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();
        final Message message = ctx.getMessage();
        final List<String> args = ctx.getArgs();

        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        String song = "https://open.spotify.com/track/43CHdbHYdcbUWP10UFKyZ7?si=EAGwnkwKQqW1P1XV7oQDqA";
        String album = "https://open.spotify.com/album/6ApYSpXF8GxZAgBTHDzYge?si=hBoC7tBeSUu-3n6AF4LNPQ";
        String playlist = "https://open.spotify.com/playlist/6FC1X11mpXhROvZnLzcwsv?si=Nj8p787AQmC4NxwodbpagQ";

//        Thread updateUsersData = new Thread(() -> {
////            final ArrayList<String> songsViaPlaylistUrl = getSongsViaPlaylistUrl(playlist);
////            final ArrayList<String> getSongsViaAlbumUrl = getSongsViaAlbumUrl(album);
////            getSongsViaAlbumUrl.forEach(System.out::println);
////            System.out.println(getSongViaUrl(song));
//        });
//        updateUsersData.start();
    }



    @Override
    public String name() {
        return "test";
    }

    @Override
    public String usage() {
        return "";
    }

    @Override
    public String description() {
        return "";
    }

    @Override
    public PermLevel permLevel() {
        return PermLevel.BOT_OWNER;
    }

    @Override
    public CommandType commandType() {
        return CommandType.HIDDEN;
    }
}
