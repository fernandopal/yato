package es.fernandopal.yato.core.audio.sources.spotify;

import com.google.api.services.youtube.model.SearchResult;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import es.fernandopal.yato.core.BotManager;
import es.fernandopal.yato.core.audio.YouTubeSearchHandler;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpotifyConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyConverter.class);
    private final SpotifyApi spotifyApi = BotManager.getSpotifyApi();


    /*
    *
    * Would be better to scrap youtube instead of this just because of the api rate limit (google can even ban my google account)
    * so in the next update I'll change it to work like that
    *
    */


    public String getSongViaUrl(@NotNull String url) {
        String trackUri = null;

        try {
            final String trackId = url.split("track/")[1].split("/")[0].split("\\?")[0];
            final Track track = spotifyApi.getTrack(trackId).build().execute();
            final String searchTerm = track.getName() + " " + track.getArtists()[0].getName();
            final List<SearchResult> searchResults = YouTubeSearchHandler.search(searchTerm, 1L);

            if(searchResults == null) { return null; }

            trackUri = "https://www.youtube.com/watch?v=" + searchResults.get(0).getId().getVideoId();

        } catch (IOException | ParseException | SpotifyWebApiException ignored) { }

        return trackUri;
    }

    public ArrayList<String> getSongsViaPlaylistUrl(@NotNull String url) {
        final ArrayList<String> youtubeUris = new ArrayList<>();

        try {
            final String playlistId = url.split("playlist/")[1].split("/")[0].split("\\?")[0];
            final Paging<PlaylistTrack> playlistTrackPaging = spotifyApi.getPlaylistsItems(playlistId).build().execute();
            final PlaylistTrack[] tracks = playlistTrackPaging.getItems();

            for (PlaylistTrack playlistTrack : tracks) {
                final Track track = (Track) playlistTrack.getTrack();
                final String searchTerm = track.getName() + " " + track.getArtists()[0].getName();
                final List<SearchResult> searchResults = YouTubeSearchHandler.search(searchTerm, 1L);

                if (searchResults == null) { return youtubeUris; }

                final String trackUri = "https://www.youtube.com/watch?v=" + searchResults.get(0).getId().getVideoId();
                youtubeUris.add(trackUri);
            }

        } catch (IOException | ParseException | SpotifyWebApiException ignored) { }

        return youtubeUris;
    }

    public ArrayList<String> getSongsViaAlbumUrl(@NotNull String url) {
        final ArrayList<String> youtubeUris = new ArrayList<>();

        try {
            final String albumId = url.split("album/")[1].split("/")[0].split("\\?")[0];
            final Paging<TrackSimplified> trackSimplifiedPaging = spotifyApi.getAlbumsTracks(albumId).build().execute();
            final TrackSimplified[] tracks = trackSimplifiedPaging.getItems();

            for (TrackSimplified track : tracks) {
                final String searchTerm = track.getName() + " " + track.getArtists()[0].getName();
                final List<SearchResult> searchResults = YouTubeSearchHandler.search(searchTerm, 1L);

                if (searchResults == null) { return youtubeUris; }

                final String trackUri = "https://www.youtube.com/watch?v=" + searchResults.get(0).getId().getVideoId();
                youtubeUris.add(trackUri);
            }

        } catch (IOException | ParseException | SpotifyWebApiException ignored) { }

        return youtubeUris;
    }
}
