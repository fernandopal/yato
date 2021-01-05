package es.fernandopal.yato.core.scheduler.tasks;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.scheduler.Task;
import es.fernandopal.yato.listeners.GuildListeners;
import es.fernandopal.yato.util.MessageUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceTimeoutTask implements Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceTimeoutTask.class);
    private final MessageUtil msg = Main.getBotManager().getMsgu();

    private final Guild guild;
    private final GuildVoiceState voiceState;

    public VoiceTimeoutTask(Guild guild) {
        this.guild = guild;
        this.voiceState = guild.getSelfMember().getVoiceState();
    }

    @Override
    public void run() {
        if(voiceState != null && voiceState.inVoiceChannel()) {
			AudioManagerController.getGuildAudioManager(guild).closeConnection();
			AudioManagerController.getGuildAudioManager(guild).destroy();
            //TODO: fix in-discord logs
//            msg.sendWebhookMsg("VC left `" + guild.toString() + "`", "https://discordapp.com/api/webhooks/733077861639913580/t_5H_mh3Onjgkki24CFTTrfNniPiXCEM-zUH7LFNDarmroQYb9zjLeH78RHSgumbD-p1");
//            LOGGER.info("VC left " + guild.toString());
        }
    }
}