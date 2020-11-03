package es.fernandopal.yato.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.audio.GuildAudioManager;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.List;

public class QueueCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();
        final Member member = ctx.getMember();
        final User user = ctx.getAuthor();

        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        final MessageUtil msg = Main.getBotManager().getMsgu();
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        long totalDuration;

        synchronized(manager.getScheduler().queue) {
            StringBuilder queue = new StringBuilder();
            int trackCount = 0;
            for(AudioTrack track : manager.getScheduler().queue) {
                queue.append("`").append(trackCount + 1).append(":` ").append(track.getInfo().title).append(" · (").append(Main.getBotManager().getTimestamp(track.getInfo().length)).append(") \n");
                trackCount++;
                if(trackCount > 9) { break; }
            }

            totalDuration = manager.getScheduler().queue.stream().mapToLong(AudioTrack::getDuration).sum();

            if(trackCount > 0) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Here are the next " + trackCount + " tracks in the queue:")
                        .setDescription(queue.toString())
                        .addField("In Queue", manager.getScheduler().queue.size() + "", true)
                        .addField("Total Duration", Main.getBotManager().getTimestamp(totalDuration), true)
                        .setFooter("Requested by " + member.getEffectiveName(), user.getEffectiveAvatarUrl());
                musicChannel.sendMessage(embed.build()).queue();
            } else {
                msg.send(musicChannel, Emoji.PAPER + " The queue currently contains `0` tracks.", Color.WHITE);
            }
        }
    }

    @Override
    public String name() {
        return "queue";
    }
    @Override
    public String usage() {
        return "{prefix}:lq";
    }
    @Override
    public String description() {
        return "Shows the next 10 songs in queue";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override
    public CommandType commandType() {
        return CommandType.MUSIC;
    }
    @Override
    public List<String> getAliases() {
        return List.of("lq", "listqueue", "queue");
    }
}
