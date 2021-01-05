package es.fernandopal.yato.listeners;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.mongodb.BasicDBObject;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.audio.TrackScheduler;
import es.fernandopal.yato.core.database.DBObjects;
import es.fernandopal.yato.core.scheduler.ScheduleHandler;
import es.fernandopal.yato.core.scheduler.tasks.VoiceTimeoutJob;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import es.fernandopal.yato.files.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class GuildListeners extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildListeners.class);
    private final MessageUtil msg = Main.getBotManager().getMsgu();
    private final Config config = new Config();

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        final Guild guild = event.getGuild();
        final ShardManager shardManager = Main.getShardManager();


        Thread thread = new Thread(() -> {
//            LOGGER.info("New guild registered: " + guild.toString() + " | Users: " + guild.getMemberCount());
            final BasicDBObject guildObject = DBObjects.getGuildObject(guild, 0L, false, 0L, config.getString("bot-prefix"), true, true);
            Main.getDb().insertGuild(guildObject);

//            LOGGER.info("Updating bot stats...");
            if (!Main.enableCompatMode) Main.getDb().updateBotUserStats(shardManager.getUsers().size(), shardManager.getGuilds().size());
            if(Main.getDiscordBotListAPI() != null) Main.getDiscordBotListAPI().setStats(shardManager.getGuilds().size());
        });
        thread.start();

        final List<String> commonNames = List.of("general", "talk", "chat", "chatting");
        EmbedBuilder suppMsg = new EmbedBuilder();
        suppMsg.setColor(Color.ORANGE);
        suppMsg.setDescription(Emoji.PAPER + "**YATO FAQ**" + Emoji.PAPER);
        suppMsg.addField("How can I get a list of commands?", "Write `y:help` or visit [this website](https://yato.fernandopal.es)", false);
        suppMsg.addField("How can I get help of an human?", "Join [our discord server](" + Main.getDb().getBotServerInvite() + ") and feel free to ping @fernandopal#8931", false);
        suppMsg.addField("How do I change the bot prefix?", "You can use `y:prefix <new prefix>`", false);
        suppMsg.setFooter("Thanks for using Yato :)", Objects.requireNonNull(Main.getShardManager().retrieveUserById(214829164253937674L)).complete().getAvatarUrl()); //TODO: changed to .complete()

        TextChannel helpChannel = null;

        for (TextChannel textChannel : guild.getTextChannels()) {
            if (textChannel.canTalk()) {
                int commonNameCounter = 0;
                for (String commonName : commonNames) {
                    if (textChannel.getName().contains(commonName)) {
                        commonNameCounter++;
                        break;
                    }
                }

                if (commonNameCounter != 0) {
                    helpChannel = textChannel;
                    helpChannel.sendMessage(suppMsg.build()).queue();
                    break;
                }
            }
        }

        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
        embed.setColor(Color.GREEN.getRGB());
        embed.setDescription(
                "**Guild joined**" + "\n" +
                        "**- Name:** " + event.getGuild().getName() + "\n" +
                        "**- Members:** " + event.getGuild().getMembers().size() + "\n" +
                        "**- Owner:** " + Objects.requireNonNull(event.getGuild().retrieveOwner().complete()).getAsMention() + "\n" +
                        "**- Sent help:** " + helpChannel
        );

        if (Main.enableCompatMode) {
            embed.setFooter(new WebhookEmbed.EmbedFooter(Main.getShardManager().getGuilds().size() + " guilds", null));
        } else {
            embed.setFooter(new WebhookEmbed.EmbedFooter(Main.getShardManager().getGuilds().size() + " guilds | " + Main.getShardManager().getUsers().size() + " users", null));
        }

        embed.build();
        msg.sendWebhookEmbed(embed, "https://discordapp.com/api/webhooks/730682513030119455/-UaLe4gErJBv9xKKBGzsjtOO3NczV_Xrjz6bxGyqd9kYutkFm3DN8gYU9pM1O3hRA4wU");
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
        embed.setColor(Color.RED.getRGB());
        embed.setDescription(
            "**Guild left**" + "\n" +
                "**- Name:** " + event.getGuild().getName() + "\n" +
                "**- Members:** " + event.getGuild().getMembers().size() + "\n"
        );

        if (Main.enableCompatMode) {
            embed.setFooter(new WebhookEmbed.EmbedFooter(Main.getShardManager().getGuilds().size() + " guilds", null));
        } else {
            embed.setFooter(new WebhookEmbed.EmbedFooter(Main.getShardManager().getGuilds().size() + " guilds | " + Main.getShardManager().getUsers().size() + " users", null));
        }

        embed.build();
        msg.sendWebhookEmbed(embed, "https://discordapp.com/api/webhooks/730682513030119455/-UaLe4gErJBv9xKKBGzsjtOO3NczV_Xrjz6bxGyqd9kYutkFm3DN8gYU9pM1O3hRA4wU");
    }



    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if(Main.isPremiumServer()) {
            final Guild guild = event.getGuild();
            final Member selfMember = event.getGuild().getSelfMember();
            final VoiceChannel channelLeft = event.getChannelLeft();

            final TrackScheduler scheduler = AudioManagerController.getGuildAudioManager(guild).getScheduler();

            if (channelLeft.getMembers().contains(selfMember)) {
                if (!hasNonBotUsers(channelLeft)) {
                    startTimeout(scheduler, guild);
                }
            }
        }
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        if(Main.isPremiumServer()) {
            final Guild guild = event.getGuild();
            final Member selfMember = event.getGuild().getSelfMember();
            final VoiceChannel channelJoined = event.getChannelJoined();

            final TrackScheduler scheduler = AudioManagerController.getGuildAudioManager(guild).getScheduler();

            if (channelJoined.getMembers().contains(selfMember)) {
                if (hasNonBotUsers(channelJoined)) {
                    cancelTimeout(scheduler, guild);
                }
            }
        }
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        if(Main.isPremiumServer()) {
            final Guild guild = event.getGuild();
            final Member selfMember = event.getGuild().getSelfMember();

            final VoiceChannel channelJoined = event.getChannelJoined();
            final VoiceChannel channelLeft = event.getChannelLeft();

            final TrackScheduler scheduler = AudioManagerController.getGuildAudioManager(guild).getScheduler();

            if (channelJoined.getMembers().contains(selfMember)) {
                if (hasNonBotUsers(channelJoined)) {
                    cancelTimeout(scheduler, guild);
                } else {
                    startTimeout(scheduler, guild);
                }
            }

            if (channelLeft.getMembers().contains(selfMember)) {
                if (!hasNonBotUsers(channelLeft)) {
                    startTimeout(scheduler, guild);
                } else {
                    cancelTimeout(scheduler, guild);
                }
            }
        }
    }

    private void startTimeout(TrackScheduler scheduler, Guild guild) {
        scheduler.setTimeout(ScheduleHandler.registerUniqueJob(new VoiceTimeoutJob(guild)));
        msg.sendWebhookMsg("VC empty | The bot will leave in 1 minute from `" + guild.toString() + "`", "https://discordapp.com/api/webhooks/733077861639913580/t_5H_mh3Onjgkki24CFTTrfNniPiXCEM-zUH7LFNDarmroQYb9zjLeH78RHSgumbD-p1");
//        LOGGER.info("VC empty | The bot will leave in 1 minute from " + guild.toString());
    }

    private void cancelTimeout(TrackScheduler scheduler, Guild guild) {
        if (scheduler.getTimeout() != null) {
            scheduler.getTimeout().cancel(true);
            scheduler.setTimeout(null);
            msg.sendWebhookMsg("VC not empty | Leave job cancelled for `" + guild.toString() + "`", "https://discordapp.com/api/webhooks/733077861639913580/t_5H_mh3Onjgkki24CFTTrfNniPiXCEM-zUH7LFNDarmroQYb9zjLeH78RHSgumbD-p1");
//            LOGGER.info("VC not empty | Leave job cancelled for " + guild.toString());
        }
    }

    private boolean hasNonBotUsers(VoiceChannel voiceChannel) {
        final List<Member> members = voiceChannel.getMembers();

        for (Member member : members) {
            if(!member.getUser().isBot()) {
                return true;
            }
        }

        return false;
    }
}
