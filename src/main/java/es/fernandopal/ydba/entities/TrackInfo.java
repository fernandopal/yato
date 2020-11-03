package es.fernandopal.ydba.entities;

public class TrackInfo {
    private final int totalPlays, todayPlays;
    private final String uri;

    public TrackInfo(int totalPlays, int todayPlays, String uri) {
        this.totalPlays = totalPlays;
        this.todayPlays = todayPlays;
        this.uri = uri;
    }

    public int getTotalPlays() {
        return totalPlays;
    }
    public int getTodayPlays() {
        return todayPlays;
    }
    public String getUri() {
        return uri;
    }
}
