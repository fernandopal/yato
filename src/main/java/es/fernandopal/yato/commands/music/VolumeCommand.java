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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class VolumeCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();

        final MessageUtil msg = Main.getBotManager().getMsgu();
        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));
        final List<String> args = ctx.getArgs();

        if(musicChannel == null) { musicChannel = tc; }

        final int cVol = manager.getPlayer().getVolume();
        if(args.size() < 1) {
            msg.sendOk(musicChannel, Emoji.VOLUME_HIGH + "Player volume: " + cVol + "%");
        } else {
            try {
                final int nVol = Integer.parseInt(args.get(0));

                if(nVol > 100 || nVol < 0) {
                    msg.sendError(musicChannel, Emoji.ERROR + " The number cant be higher than 100 or smaller than 0");
                    return;
                }

                manager.getPlayer().setVolume(nVol);

                String vEmoji = (nVol < cVol) ? Emoji.VOLUME_LOW : Emoji.VOLUME_HIGH;

                msg.sendOk(musicChannel, vEmoji + " Player volume has been set to " + nVol + "%");
            } catch(NumberFormatException e) {
                msg.sendError(musicChannel, Emoji.WARNING + " You must insert a number between 0 and 100");
            }
        }
    }

    @Override public String name() {
        return "volume";
    }
    @Override public String usage() {
        return "{prefix}:volume";
    }
    @Override public String description() {
        return "Change the global volume of the player for your server";
    }
    @Override public PermLevel permLevel() {
        return PermLevel.GUILD_DJ;
    }
    @Override public CommandType commandType() {
        return CommandType.MUSIC;
    }
    @Override public List<String> getAliases() {
        return List.of("v", "vol");
    }
}
