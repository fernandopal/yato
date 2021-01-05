package es.fernandopal.yato;

import ch.qos.logback.classic.Level;
import es.fernandopal.yato.core.BotManager;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.scheduler.DailyTasks;
import es.fernandopal.yato.core.web.YatoRestServer;
import es.fernandopal.yato.core.web.routes.GetGuild;
import es.fernandopal.yato.core.web.routes.GetShards;
import es.fernandopal.yato.core.web.routes.GetStats;
import es.fernandopal.yato.core.web.routes.GetUser;
import es.fernandopal.yato.files.Webserver;
import es.fernandopal.yato.listeners.CommandListener;
import es.fernandopal.yato.listeners.GuildListeners;
import es.fernandopal.yato.listeners.ConnectionListeners;
import es.fernandopal.yato.files.Config;
import es.fernandopal.ydba.database.DatabaseManager;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.discordbots.api.client.DiscordBotListAPI;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URISyntaxException;
import java.util.List;

@SuppressWarnings("InstantiationOfUtilityClass")
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static boolean maintenance = false;

    private static final OperatingSystemMXBean system = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final RuntimeMXBean runtime_mx = ManagementFactory.getRuntimeMXBean();
    private static final YatoRestServer restServer = new YatoRestServer();
    private static final Runtime runtime = Runtime.getRuntime();
    private static final Config config = new Config(); //TODO: I need to make a getter to have access from other classes and prevent to have it declared multiple times
    private static BotManager botManager;
    private static ShardManager shardManager;
    private static DiscordBotListAPI discordBotListAPI;
    private static DatabaseManager db;

    public static final boolean enableCompatMode = true;

    public static void main(String[] args) throws LoginException, URISyntaxException {
        ch.qos.logback.classic.Logger mongoLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.mongodb.driver");
        ch.qos.logback.classic.Logger lavaLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("lavalink.client.io");
        ch.qos.logback.classic.Logger jdaLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("net.dv8tion.jda");
        mongoLogger.setLevel(Level.WARN);
        lavaLogger.setLevel(Level.ERROR);
        jdaLogger.setLevel(Level.INFO);

        db = new DatabaseManager();
        botManager = new BotManager();
        shardManager = botManager.getShardManager();

        LOGGER.info("Registering listeners...");
        registerListeners();

        LOGGER.info("Loading AudioManagerController...");
        new AudioManagerController();

        LOGGER.info("Starting DailyTasks...");
        final DailyTasks dailyTasks = new DailyTasks();
        dailyTasks.executeEveryMorning();
        dailyTasks.executeEveryNight();

        if(config.getString("dbl-token") == null) { //The bot-id can't be null at this point so we don't have to check it
            LOGGER.warn("Top.gg token not found, disabling voting system");
            discordBotListAPI = null;
        } else {
            LOGGER.info("Connecting with top.gg");
            discordBotListAPI = new DiscordBotListAPI.Builder().token(config.getString("dbl-token")).botId(config.getString("bot-id")).build();
        }

        restServer.listen();
    }

    private static void registerListeners() {
        shardManager.addEventListener(
                new CommandListener(),
                new GuildListeners(),
                new ConnectionListeners()
        );
    }

    public static String getPrefix(long guildId) {
        return db.getPrefix(guildId);
    }
    public static BotManager getBotManager() {
        return botManager;
    }
    public static ShardManager getShardManager() {
        return shardManager;
    }
    public static TextChannel getTextChannelById(long channelId) {
        return shardManager.getTextChannelById(channelId);
    }
    public static Guild getGuildById(long guildId) { return shardManager.getGuildById(guildId); }
    public static Guild getGuildById(String guildId) { return shardManager.getGuildById(guildId); }
    public static User getUserById(long userId) { return shardManager.retrieveUserById(userId).complete(); }
    public static User getUserById(String userId) { return shardManager.retrieveUserById(userId).complete(); }
    public static DiscordBotListAPI getDiscordBotListAPI() { return discordBotListAPI; }
    public static @NotNull List<Guild> getMutualGuilds(User user) { return shardManager.getMutualGuilds(user); }
    public static DatabaseManager getDb() {
        return db;
    }
    public static Runtime getRuntime() {
        return runtime;
    }
    public static OperatingSystemMXBean getSystem() {
        return system;
    }
    public static RuntimeMXBean getRuntime_mx() {
        return runtime_mx;
    }

    //TODO: Implement an option to enable/disable donations to unlock certain features, this can also have a config file to choose which features are
    // unlocked by donations and which ones are not. My plan is to have a system that relies on per server donations, just for server owners but that
    // also allow the users of that server to contribute to a global goal and if that goal is completed every month, the server will receive all the
    // extra features.
    public static boolean isPremiumServer() {
        return true; //Return true until implemented so everything is free
    }
}
