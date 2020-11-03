package es.fernandopal.yato.commands.rewards;

import es.fernandopal.yato.core.HarunaRequest;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import es.fernandopal.ydba.database.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.discordbots.api.client.DiscordBotListAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class VoteCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoteCommand.class);

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel tc = ctx.getChannel();
        final MessageUtil msg = new MessageUtil();
        final Member member = ctx.getMember();

        final DiscordBotListAPI discordBotListAPI = Main.getDiscordBotListAPI();
        final DatabaseManager db = Main.getDb();

        HarunaRequest haruna = new HarunaRequest("http://cdn.fernandopal.es:6969", "BOFA938JIPERL00B");

        if(haruna.hasVoted(member.getIdLong())) {
            if(db.hasVoted(member.getIdLong())) {
                msg.sendWarn(tc, Emoji.CLOCK1 + " You have already voted today, come again in 12h to get more points!");
            } else {
                AtomicInteger pointsMultiplier = new AtomicInteger(1);
                discordBotListAPI.getVotingMultiplier().whenComplete((m, ignored) -> { pointsMultiplier.set(m.isWeekend() ? 3 : 1); });

                final int multiplier = pointsMultiplier.intValue();
                final int randomPoints = ThreadLocalRandom.current().nextInt(multiplier, (10 * multiplier) + multiplier);

                db.registerDailyVote(member.getIdLong(), randomPoints);
                db.setTotalPoints(member.getIdLong(), (db.getTotalPoints(member.getIdLong()) + randomPoints));

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.GREEN);
                embedBuilder.setDescription(
                        Emoji.CHECK + "Your vote has been registered." + "\n" +
                        "- Points obtained: " + randomPoints + "\n" +
                        "- Your points: " + db.getTotalPoints(member.getIdLong())
                );
                tc.sendMessage(embedBuilder.build()).queue();
            }
        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.BLUE);
            embedBuilder.setDescription(
                    "Steps to get your daily points:" + "\n" +
                    "1. Go to [this link](https://top.gg/bot/454272495114256394) and click on the 'vote' button." + "\n" +
                    "2. When that's done and the page says you have voted come back here and execute this command again. (It can take a minute to register the vote, please be patient)"
            );
            tc.sendMessage(embedBuilder.build()).queue();
        }
    }

//    private boolean hasVoted(long userId) {
//        try {
//            HttpClient client = HttpClient.newHttpClient();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://cdn.fernandopal.es:6969/voteInfo?user_id=" + userId))
//                    .header("authorization", "BOFA938JIPERL00B")
//                    .build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if(response != null && !response.body().isEmpty()) {
//                JSONObject json = new JSONObject(response.body());
//                if(json.getLong("user") == userId) {
//                    return true;
//                }
//            }
//
//            return true;
//        } catch (IOException | InterruptedException e) { e.printStackTrace(); }
//
//        return false;
//    }

    @Override
    public String name() {
        return "vote";
    }
    @Override
    public String usage() {
        return "{prefix}:vote";
    }
    @Override
    public String description() {
        return "Vote the bot and get points that you can use to buy perks on the bot store";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override
    public CommandType commandType() {
        return CommandType.REWARDS;
    }
}
