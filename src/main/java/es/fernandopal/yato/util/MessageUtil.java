package es.fernandopal.yato.util;

import java.awt.Color;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import es.fernandopal.yato.listeners.ConnectionListeners;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtil.class);

	public void sendError(TextChannel tc, String msg) {
		this.send(tc, msg, Color.RED);
	}
	public void sendWarn(TextChannel tc, String msg) {
		this.send(tc, msg, Color.YELLOW);
	}
	public void sendOk(TextChannel tc, String msg) {
		this.send(tc, msg, Color.GREEN);
	}
	
	public void sendPm(PrivateChannel pc, String msg, Color color) {
		try {
			pc.sendMessage(new EmbedBuilder().setColor(color).setDescription(msg).build()).queue();
		} catch(Exception e) { LOGGER.error("Error trying to send a private message:"); e.printStackTrace(); }
	}
	
	public void send(TextChannel tc, String msg, Color color) {
		try {
			tc.sendMessage(new EmbedBuilder().setColor(color).setDescription(msg).build()).queue();
		} catch(Exception e) { LOGGER.error("Error trying to send a message:"); e.printStackTrace();  }
	}
	
	public void send(TextChannel tc, String msg, Color color, boolean selfDelete) {
		try {
			if (selfDelete) {
				tc.sendMessage(new EmbedBuilder().setColor(color).setDescription(msg).build()).queue(m -> m.delete().queue());
			} else {
				tc.sendMessage(new EmbedBuilder().setColor(color).setDescription(msg).build()).queue();
			}
		} catch(Exception e) { LOGGER.error("Error trying to send a message:"); e.printStackTrace();  }
	}

//	TODO: fix in-discord logs
//	public void sendWebhookEmbed(@NotNull WebhookEmbedBuilder content, String webhookUrl) {
//		WebhookClient client;
//		WebhookClientBuilder builder = new WebhookClientBuilder(webhookUrl);
//		builder.setThreadFactory((job) -> {
//			Thread thread = new Thread(job);
//			thread.setName("Webhook-Thread");
//			thread.setDaemon(true);
//			return thread;
//		});
//
//		client = builder.build();
//		client.send(content.build());
//	}

//	public void sendWebhookMsg(@NotNull String content, String webhookUrl) {
//		WebhookClient client;
//		WebhookClientBuilder builder = new WebhookClientBuilder(webhookUrl);
//		builder.setThreadFactory((job) -> {
//			Thread thread = new Thread(job);
//			thread.setName("Webhook-Thread");
//			thread.setDaemon(true);
//			return thread;
//		});
//
//		client = builder.build();
//		client.send(content);
//	}
}
