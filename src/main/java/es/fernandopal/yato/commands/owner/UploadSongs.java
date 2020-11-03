package es.fernandopal.yato.commands.owner;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.files.Config;
import es.fernandopal.ydba.entities.TrackInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadSongs implements ICommand {
    private final Config config = new Config();

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final String playlistId = args.get(0);


        Thread thread = new Thread(() -> {
            final YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {}).setApplicationName("yato").build();
            YouTube.PlaylistItems.List playlistItemRequest;

            try {
                List<PlaylistItem> playlistItemList = new ArrayList<>();

                playlistItemRequest = youtube.playlistItems().list("snippet,contentDetails");
                playlistItemRequest.setPlaylistId(playlistId);
                playlistItemRequest.setFields("items(snippet/title,snippet/description,snippet/thumbnails/default/url,contentDetails/videoId),nextPageToken,pageInfo");
                playlistItemRequest.setKey(config.getString("google-api-key"));
                String nextToken = "";
                do {

                    playlistItemRequest.setPageToken(nextToken);
                    PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                    playlistItemList.addAll(playlistItemResult.getItems());

                    nextToken = playlistItemResult.getNextPageToken();
                } while (nextToken != null);


                for (PlaylistItem i : playlistItemList) {
                    TrackInfo trackInfo = new TrackInfo(0, 0, "https://www.youtube.com/watch?v=" + i.getContentDetails().getVideoId());
                    Main.getDb().insertSong(trackInfo);
                }

                ctx.getChannel().sendMessage("Uploaded: " + playlistItemList.size() + " songs.").queue();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @Override
    public String name() {
        return "uploadsongs";
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
