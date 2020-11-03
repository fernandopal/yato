package es.fernandopal.ydba.entities;

import java.util.UUID;

public class YTrack implements java.io.Serializable {
    private final String id;
    private final String trackB64;
    private final String trackUri;
    private final String trackName;
    private final int totalPlays, todayPlays;

    public YTrack(String id, String trackUri, String trackB64, String trackName, int totalPlays, int todayPlays) {
        this.id = id;
        this.trackB64 = trackB64;
        this.trackUri = trackUri;
        this.trackName = trackName;
        this.totalPlays = totalPlays;
        this.todayPlays = todayPlays;
    }

    public YTrack(String trackUri, String trackB64, String trackName, int totalPlays, int todayPlays) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.trackB64 = trackB64;
        this.trackUri = trackUri;
        this.trackName = trackName;
        this.totalPlays = totalPlays;
        this.todayPlays = todayPlays;
    }

    public String getTrackId() {
        return id;
    }

    public String getTrackUri() {
        return trackUri;
    }

    public String getTrackB64() {
        return trackB64;
    }

    public int getTotalPlays() {
        return totalPlays;
    }

    public int getTodayPlays() {
        return todayPlays;
    }

    public String getTrackName() {
        return trackName;
    }
}
