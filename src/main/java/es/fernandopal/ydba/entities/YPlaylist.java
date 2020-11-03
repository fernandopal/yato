package es.fernandopal.ydba.entities;

import java.util.ArrayList;
import java.util.UUID;

public class YPlaylist implements java.io.Serializable {
    protected final String id;
    protected final ArrayList<String> playlistTracks;
    protected final String playlistName;
    protected final long playlistAuthor;
    protected final boolean isPrivate;

    public YPlaylist(String id, ArrayList<String> playlistTracks, String playlistName, long playlistAuthor, boolean isPrivate) {
        this.id = id;
        this.playlistTracks = playlistTracks;
        this.playlistName = playlistName;
        this.playlistAuthor = playlistAuthor;
        this.isPrivate = isPrivate;
    }

    public YPlaylist(ArrayList<String> playlist_tracks, String playlistName, long playlistAuthor, boolean isPrivate) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.playlistTracks = playlist_tracks;
        this.playlistName = playlistName;
        this.playlistAuthor = playlistAuthor;
        this.isPrivate = isPrivate;
    }

    public YPlaylist(String playlistName, long playlistAuthor, boolean isPrivate) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.playlistTracks = new ArrayList<>();
        this.playlistName = playlistName;
        this.playlistAuthor = playlistAuthor;
        this.isPrivate = isPrivate;
    }

    public String getPlaylistId() {
        return id;
    }

    public ArrayList<String> getPlaylistTracks() {
        return playlistTracks;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public long getPlaylistAuthor() {
        return playlistAuthor;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}
