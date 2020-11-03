package es.fernandopal.yato.commands.music;

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

import java.util.List;

public class RemoveCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();
        final List<String> args = ctx.getArgs();

        final MessageUtil msg = Main.getBotManager().getMsgu();
        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        if(args.size() < 1) {
            msg.sendError(musicChannel, "Please insert the position in the queue of the song that you want to remove ``");
        }

        try {
//            final int i = Integer.parseInt(args.get(0));
//
//            if(manager.getScheduler().removeFromQueue(i)) {
//                msg.sendOk(tc, Emoji.PAPER + " The song that was on the position " + i + " of the queue was removed.");
//            } else {
//                msg.sendError(tc, Emoji.ERROR + " The number that you have inserted is bigger than the queue size.");
//            }
        } catch (NumberFormatException e) {
            msg.sendError(tc, Emoji.ERROR + " Please insert a number.");
        }

    }

    @Override public String name() {
        return "remove";
    }
    @Override public String usage() {
        return "{prefix}:remove <position in queue>";
    }
    @Override public String description() {
        return "Remove a song of the queue";
    }
    @Override public PermLevel permLevel() {
        return PermLevel.GUILD_DJ;
    }
    @Override public CommandType commandType() {
        return CommandType.MUSIC;
    }
}
