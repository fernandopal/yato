package es.fernandopal.yato.core.scheduler.tasks;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.scheduler.Task;
import es.fernandopal.ydba.entities.YUser;

public class SelectedPlaylistTimeoutTask implements Task {
    private final long userId;
    private final String yPlaylist;

    public SelectedPlaylistTimeoutTask(long userId, String yPlaylist) {
        this.userId = userId;
        this.yPlaylist = yPlaylist;
    }

    @Override
    public void run() {
        try {
            final YUser yUser = new YUser(userId, yPlaylist);
            if(!Main.getDb().insertUser(yUser)) {
                Main.getDb().selectPlaylist(yUser.getUserId(), null);
            }
        } catch (Exception ignored) { }
    }
}