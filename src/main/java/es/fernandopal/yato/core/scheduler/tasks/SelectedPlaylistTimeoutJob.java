package es.fernandopal.yato.core.scheduler.tasks;

import es.fernandopal.yato.core.scheduler.Job;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.TimeUnit;

public class SelectedPlaylistTimeoutJob extends Job {
    private final SelectedPlaylistTimeoutTask selectedPlaylistTimeoutTask;

    public SelectedPlaylistTimeoutJob(long userId, String yPlaylist) {
        super(15, 0, TimeUnit.MINUTES);
        selectedPlaylistTimeoutTask = new SelectedPlaylistTimeoutTask(userId, yPlaylist);
    }

    @Override
    public void run() {
        handleTask(selectedPlaylistTimeoutTask);
    }
}