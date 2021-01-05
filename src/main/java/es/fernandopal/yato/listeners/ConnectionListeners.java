package es.fernandopal.yato.listeners;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.mongodb.BasicDBObject;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.BotManager;
import es.fernandopal.yato.core.database.DBObjects;
import es.fernandopal.yato.util.MessageUtil;
import es.fernandopal.yato.files.Config;
import lavalink.client.io.LavalinkSocket;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ConnectionListeners extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionListeners.class);

    private static int readyShards = 0;
    private final MessageUtil msg = Main.getBotManager().getMsgu();
    private final Config config = new Config();

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        readyShards++;
        if (readyShards == config.getInt("bot-shards")) {
            Main.getShardManager().setStatus(OnlineStatus.ONLINE);
            Main.getShardManager().setActivity(Activity.playing(config.getString("bot-game")));

            Thread updateGuildsData = new Thread(() -> {
                LOGGER.info("Running guilds task...");
                for (Guild guild : Main.getShardManager().getGuilds()) {

                    final BasicDBObject guildObject = DBObjects.getGuildObject(guild, 0L, false, 0L, config.getString("bot-prefix"), true, true);
                    Main.getDb().insertGuild(guildObject);
                }

                LOGGER.info("Guilds processed: " + Main.getShardManager().getGuilds().size());
            });
            updateGuildsData.start();

            Thread updateUsersData = new Thread(() -> {
                LOGGER.info("Running users task...");
                int users = 0;
                for(Guild g : Main.getShardManager().getGuilds()) { users += g.loadMembers().get().size(); }
                Main.getDb().updateBotUserStats(users, Main.getShardManager().getGuilds().size());
                if(Main.getDiscordBotListAPI() != null) Main.getDiscordBotListAPI().setStats(Main.getShardManager().getGuilds().size());
                LOGGER.info("Users processed: " + users);
            });
            if(!Main.enableCompatMode) updateUsersData.start();

            Thread updateBotInfo = new Thread(() -> {
                LOGGER.info("Running bot info task...");
                Main.getDb().updateBotShardsCount(config.getInt("bot-shards"));
                LOGGER.info("Bot info updated!");
            });
            updateBotInfo.start();

            int availableNodes = 0;
            for (LavalinkSocket n : Main.getBotManager().getLavalink().getNodes()) {
                if (n.isAvailable()) { availableNodes++; }
            }

            if(availableNodes == 0) {
                LOGGER.warn("You have 0 lavalink nodes available, this means that we can't send audio to discord.");
            } else {
                LOGGER.info("Lavalink available nodes: " + availableNodes);
            }

            BotManager.ready = true;

            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
            embed.setColor(Color.GREEN.getRGB());

            final String uptime = Main.getBotManager().getDurationBreakdown(Main.getRuntime_mx().getUptime(), false);

            embed.setDescription("**" + event.getJDA().getSelfUser().getName() + " || Startup took " + uptime + "**");

            final LocalDateTime CET = LocalDateTime.now(ZoneId.of("CET"));
            final String date = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(CET);
            final String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(CET);

            embed.setFooter(new WebhookEmbed.EmbedFooter(date + " [" + time + " CET]", event.getJDA().getSelfUser().getEffectiveAvatarUrl()));
            embed.build();
            msg.sendWebhookEmbed(embed, "https://discordapp.com/api/webhooks/733019361601650880/V_tUSYJgeGR1MkESRQAZbRZStvpirj2pC5o8bXayho9shcPa2xMXQl8SQ-bgqVlfNLzW");
        }
    }

    @Override
    public void onDisconnect(@Nonnull DisconnectEvent event) {
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
        embed.setColor(Color.getHSBColor(5, 73, 96).getRGB());
        embed.setDescription("**" + event.getJDA().getSelfUser().getName() + " || Disconnected from WS**");
        embed.addField(new WebhookEmbed.EmbedField(false, "Code:", Objects.requireNonNull(event.getCloseCode()).toString()));

        final LocalDateTime CET = LocalDateTime.now(ZoneId.of("CET"));
        final String date = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(CET);
        final String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(CET);

        embed.setFooter(new WebhookEmbed.EmbedFooter(date + " [" + time + " CET]", event.getJDA().getSelfUser().getEffectiveAvatarUrl()));
        embed.build();
        msg.sendWebhookEmbed(embed, "https://discordapp.com/api/webhooks/733019361601650880/V_tUSYJgeGR1MkESRQAZbRZStvpirj2pC5o8bXayho9shcPa2xMXQl8SQ-bgqVlfNLzW");
    }

    @Override
    public void onReconnect(@Nonnull ReconnectedEvent event) {
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
        embed.setColor(Color.GREEN.getRGB());
        embed.setDescription("**" + event.getJDA().getSelfUser().getName() + " || Reconnected to WS**");

        final LocalDateTime CET = LocalDateTime.now(ZoneId.of("CET"));
        final String date = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(CET);
        final String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(CET);

        embed.setFooter(new WebhookEmbed.EmbedFooter(date + " [" + time + " CET]", event.getJDA().getSelfUser().getEffectiveAvatarUrl()));
        embed.build();
        msg.sendWebhookEmbed(embed, "https://discordapp.com/api/webhooks/733019361601650880/V_tUSYJgeGR1MkESRQAZbRZStvpirj2pC5o8bXayho9shcPa2xMXQl8SQ-bgqVlfNLzW");
    }

    @Override
    public void onResume(@Nonnull ResumedEvent event) {
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
        embed.setColor(Color.GREEN.getRGB());
        embed.setDescription("**" + event.getJDA().getSelfUser().getName() + " || WS connection resumed**");

        final LocalDateTime CET = LocalDateTime.now(ZoneId.of("CET"));
        final String date = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(CET);
        final String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(CET);

        embed.setFooter(new WebhookEmbed.EmbedFooter(date + " [" + time + " CET]", event.getJDA().getSelfUser().getEffectiveAvatarUrl()));
        embed.build();
        msg.sendWebhookEmbed(embed, "https://discordapp.com/api/webhooks/733019361601650880/V_tUSYJgeGR1MkESRQAZbRZStvpirj2pC5o8bXayho9shcPa2xMXQl8SQ-bgqVlfNLzW");
    }
}
