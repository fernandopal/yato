package es.fernandopal.yato.core;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.wrapper.spotify.SpotifyApi;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.audio.sources.spotify.SpotifyApiManager;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import es.fernandopal.yato.files.Config;
import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

public class BotManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotManager.class);
    private final EventWaiter eventWaiter = new EventWaiter();
    private final MessageUtil msg = new MessageUtil();
    private final ShardManager shardManager;
    private final JdaLavalink lavalink;
    private static SpotifyApi spotifyApi;
    public static boolean ready = false;

    public BotManager() throws LoginException, URISyntaxException {
        final Config config = new Config();

        //SpotifyApi
        SpotifyApiManager.initialize(config.getString("spotify-client-id"), config.getString("spotify-secret"));
        spotifyApi = SpotifyApiManager.getSpotifyApi();

        //Lavalink
        LOGGER.info("Loading Lavalink...");
        this.lavalink = new JdaLavalink(config.getString("bot-id"), config.getInt("bot-shards"), this::getShardById);
        this.lavalink.setAutoReconnect(true);

        //JavaDiscordApi
        LOGGER.info("Loading JDA...");
        DefaultShardManagerBuilder dsmb = DefaultShardManagerBuilder.createDefault(
            config.getString("bot-token"),
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
        );

        if(!Main.enableCompatMode) dsmb.enableIntents(GatewayIntent.GUILD_MEMBERS);

        dsmb.disableCache(CacheFlag.CLIENT_STATUS, CacheFlag.ACTIVITY, CacheFlag.MEMBER_OVERRIDES); //Added MEMBER_OVERRIDES            (BN: 2.0_3)
//        dsmb.setChunkingFilter(ChunkingFilter.ALL);                                               //Removed ChunkingFilter.ALL        (BN: 2.0_3)

        if(!Main.enableCompatMode) dsmb.setMemberCachePolicy(MemberCachePolicy.ALL);

        dsmb.setShardsTotal(config.getInt("bot-shards"));
        dsmb.addEventListeners(this.lavalink);
        dsmb.addEventListeners(this.eventWaiter);
        dsmb.setVoiceDispatchInterceptor(this.lavalink.getVoiceInterceptor());
        this.shardManager = dsmb.build();

        this.shardManager.setStatus(OnlineStatus.IDLE);
        this.shardManager.setActivity(Activity.playing("Starting bot..."));

        LOGGER.info("Registering lavalink nodes");
        final ArrayList<Object> nodes = config.getList("lavalink-nodes");
        for (Object n : nodes) {
            final String[] node = ((String) n).split("@");
            lavalink.addNode(new URI("ws://" + node[0]), node[1]);
        }
    }

    public String getTimestamp(long duration) {
        int seconds = (int) (duration / 1000) % 60 ;
        int minutes = (int) ((duration / (1000 * 60)) % 60);
        int hours   = (int) ((duration / (1000 * 60 * 60)) % 24);
        int days    = (int) ((duration / (1000 * 60 * 60 * 24)));

        if(days > 0) {
            return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        } else if(hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public String getStatusAsEmoji(int shard_id) {
        String statusEmoji = Emoji.STATUS_IDLE;
        JDA.Status status = getStatus(shard_id);

        if(status == JDA.Status.CONNECTED) {
            statusEmoji = Emoji.STATUS_ONLINE;
        } else if(status == JDA.Status.SHUTDOWN) {
            statusEmoji = Emoji.STATUS_DND;
        } else if(status == JDA.Status.FAILED_TO_LOGIN) {
            statusEmoji = Emoji.STATUS_DND;
        } else if(status == JDA.Status.DISCONNECTED) {
            statusEmoji = Emoji.STATUS_DND;
        }

        return statusEmoji;
    }

    public JDA.Status getStatus(int shard_id) {
        return Objects.requireNonNull(shardManager.getShardById(shard_id)).getStatus();
    }

    public String getDurationBreakdown(long millis, final boolean showMS) {
        if (millis <= 0) {
            return "-";
        }

        final long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        final long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(seconds);

        final StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days);
            sb.append("d ");
        }
        if (hours > 0) {
            sb.append(String.format("%02d", hours));
            sb.append("h ");
        }
        if (minutes > 0) {
            sb.append(String.format("%02d", minutes));
            sb.append("m ");
        }
        if (seconds > 0) {
            sb.append(String.format("%02d", seconds));
            sb.append("s ");
        }
        if ((seconds <= 0) && (millis > 0) && showMS) {
            sb.append(String.format("%02d", millis));
            sb.append("ms");
        }

        return sb.toString();
    }

//    public JDA.Status getShardStatus(int shard_id) { return this.shardManager.getStatus(shard_id); }
//    public List<User> getAllUsers() { return this.shardManager.getUsers(); }
//    public List<User> getUsersByShard(int shard_id) { return Objects.requireNonNull(this.shardManager.getShardById(shard_id)).getUsers(); }
//    public List<Guild> getAllGuilds() { return this.shardManager.getGuilds(); }
//    public List<Guild> getGuildsByShard(int shard_id) { return Objects.requireNonNull(this.shardManager.getShardById(shard_id)).getGuilds(); }
//    public Guild getGuildById(String discordServerId) { return shardManager.getGuildById(discordServerId); }
//    public Guild getGuildById(long discordServerId) { return shardManager.getGuildById(discordServerId); }
//    public double getAveragePing() { return this.shardManager.getAverageGatewayPing(); }
//    public double getPingForShard(int shard_id) { return Objects.requireNonNull(this.shardManager.getShardById(shard_id)).getGatewayPing(); }
//    public void setGame(Activity activity) { this.shardManager.setActivity(activity); }
//    public void setStatus(OnlineStatus status) { this.shardManager.setStatus(status); }
    public JDA getShardById(int shard_id) {
        return this.shardManager.getShardById(shard_id);
    }
    public ShardManager getShardManager() {
        return this.shardManager;
    }
    public JdaLavalink getLavalink() {
        return lavalink;
    }
    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }
    public MessageUtil getMsgu() {
        return this.msg;
    }
//    public boolean isReady() { return ready; }
    public static SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }
}
