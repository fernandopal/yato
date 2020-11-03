package es.fernandopal.yato.core.scheduler;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.mongodb.BasicDBObject;
import es.fernandopal.yato.core.database.DBObjects;
import es.fernandopal.yato.files.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.fernandopal.yato.Main;

public class DailyTasks {
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyTasks.class);
	private final Config config = new Config();
	private final ShardManager shardManager = Main.getShardManager();

	public void executeEveryMorning() {
		Calendar morning = Calendar.getInstance();
		morning.set(Calendar.HOUR_OF_DAY, 11);
		morning.set(Calendar.MINUTE, 59);
		morning.set(Calendar.SECOND, 59);

		Timer timer = new Timer();
		timer.schedule(new morningTasks(), morning.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
	}

	public void executeEveryNight() {
		Calendar night = Calendar.getInstance();
		night.set(Calendar.HOUR_OF_DAY, 23);
		night.set(Calendar.MINUTE, 59);
		night.set(Calendar.SECOND, 59);

		Timer timer = new Timer();
		timer.schedule(new nightTasks(), night.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
	}

	static class morningTasks extends TimerTask {
		public void run() {
			Thread cleanDailyVotes = new Thread(() -> {
				LOGGER.info("Clean daily votes (morning) ... Done [Multithreaded]");
				Main.getDb().resetDailyVotes();
			});
			cleanDailyVotes.start();
		}
	}
	
	class nightTasks extends TimerTask {
		public void run() {
			LOGGER.info("Running daily tasks [...]");
			Thread cleanSongs = new Thread(() -> {
				LOGGER.info("Clean songs daily reproductions ... Done [Multithreaded]");
				Main.getDb().resetDailyPlays();
			});
            cleanSongs.start();

			Thread cleanDailyVotes = new Thread(() -> {
				LOGGER.info("Clean daily votes (night) ... Done [Multithreaded]");
				Main.getDb().resetDailyVotes();
			});
			cleanDailyVotes.start();

			Thread updateGuildsData = new Thread(() -> {
				LOGGER.info("Running guilds task...");
				for (Guild guild : shardManager.getGuilds()) {
					final BasicDBObject guildObject = DBObjects.getGuildObject(guild, 0L, false, 0L, config.getString("bot-prefix"), true, true);
					Main.getDb().insertGuild(guildObject);
				}

				LOGGER.info("Guilds processed: " + shardManager.getGuilds().size());
			});
			updateGuildsData.start();

			Thread updateUsersData = new Thread(() -> {
				LOGGER.info("Running users task...");
				int users = 0;
				for(Guild g : shardManager.getGuilds()) { users += g.getMemberCount(); }
				Main.getDb().updateBotUserStats(users, shardManager.getGuilds().size());
				Main.getDiscordBotListAPI().setStats(shardManager.getGuilds().size());
				LOGGER.info("Users processed: " + users);
			});
			updateUsersData.start();

			Thread updateBotInfo = new Thread(() -> {
				LOGGER.info("Running bot info task...");
				Main.getDb().updateBotShardsCount(config.getInt("bot-shards"));
				LOGGER.info("Bot info updated!");
			});
			updateBotInfo.start();
		}
	}
}
