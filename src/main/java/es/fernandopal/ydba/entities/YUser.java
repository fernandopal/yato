package es.fernandopal.ydba.entities;

public class YUser implements java.io.Serializable {
    private final long id;
    private final String selectedPlaylist;
    private final int totalPoints;

    public YUser(long id, String selectedPlaylist, int totalPoints) {
        this.id = id;
        this.selectedPlaylist = selectedPlaylist;
        this.totalPoints = totalPoints;
    }

    public YUser(long id, String selectedPlaylist) {
        this.id = id;
        this.selectedPlaylist = selectedPlaylist;
        this.totalPoints = 0;
    }

    public long getUserId() {
        return id;
    }

    public String getSelectedPlaylist() {
        return selectedPlaylist;
    }

    public int getTotalPoints() {
        return totalPoints;
    }
}
