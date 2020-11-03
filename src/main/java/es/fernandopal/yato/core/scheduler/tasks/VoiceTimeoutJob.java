package es.fernandopal.yato.core.scheduler.tasks;

import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.TimeUnit;

import es.fernandopal.yato.core.scheduler.Job;

public class VoiceTimeoutJob extends Job {
    private final VoiceTimeoutTask voiceTimeoutTask;

    public VoiceTimeoutJob(Guild guild) {
        super(1, 0, TimeUnit.MINUTES);
        voiceTimeoutTask = new VoiceTimeoutTask(guild);
    }

    @Override
    public void run() {
        handleTask(voiceTimeoutTask);
    }
}