package es.fernandopal.yato.commands.music;

import com.jagrosh.jdautilities.menu.ButtonMenu;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.audio.GuildAudioManager;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import es.fernandopal.ydba.database.DatabaseManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class SkipCommand implements ICommand {

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();
        final List<String> args = ctx.getArgs();

        final MessageUtil msg = Main.getBotManager().getMsgu();
        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        if(manager.getPlayer().getPlayingTrack() == null) {
            msg.sendError(musicChannel, "There is no track to skip.");
            return;

        }

        if(args.size() < 1) {
            msg.sendOk(musicChannel, Emoji.NEXT_TRACK + " Track `" + manager.getPlayer().getPlayingTrack().getInfo().title + "` has been skipped.");
            manager.getScheduler().nextTrack();

        } else {
            try {
                final int nSongs = Integer.parseInt(args.get(0));
                final int qSize = manager.getScheduler().queue.size();

                if(nSongs > qSize) {
                    final String NEXT_TRACK = Emoji.reactFromCustom(Emoji.NEXT_TRACK);
                    final String CROSS = Emoji.reactFromCustom(Emoji.CROSS);

                    final TextChannel finalMusicChannel = musicChannel;
                    final ButtonMenu confirmMenu = new ButtonMenu.Builder()
                        .setDescription(
                                "You have `" + qSize + "` songs in queue but you have tried to skip `" + nSongs + "`.\n" +
                                "React with `" + Emoji.NEXT_TRACK + "` if you want me to clear the queue instead or with `" + Emoji.CROSS + "` to cancel."
                        )
                        .setColor(Color.ORANGE)
                        .setChoices(NEXT_TRACK, CROSS)
                        .setEventWaiter(Main.getBotManager().getEventWaiter())
                        .setAction(re -> {
                            final String name = re.getName();
                            if (name.equals(NEXT_TRACK)) {
                                manager.getScheduler().clearQueue();
                                msg.sendOk(finalMusicChannel, Emoji.PAPER + " Queue cleared.");

                            }

                        })
                        .setFinalAction(m -> { try { m.clearReactions().queue(); } catch (PermissionException ignore) { } })
                        .build();

                    confirmMenu.display(musicChannel);
                    return;

                }

                for (int i = 0; i < nSongs; i++) { manager.getScheduler().nextTrack(); }
                msg.sendOk(musicChannel, Emoji.NEXT_TRACK + " Skipped `" + nSongs + "` tracks.");

            } catch (NumberFormatException e) {
                msg.sendError(musicChannel, "Please insert a number of songs to skip");

            }

        }


    }

    @Override
    public String name() {
        return "skip";
    }
    @Override
    public String usage() {
        return "{prefix}:skip";
    }
    @Override
    public String description() {
        return "Skip the current song";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_DJ;
    }
    @Override
    public CommandType commandType() {
        return CommandType.MUSIC;
    }
    @Override
    public List<String> getAliases() {
        return List.of("s");
    }
}
