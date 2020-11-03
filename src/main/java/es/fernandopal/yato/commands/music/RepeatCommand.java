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

public class RepeatCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();

        final MessageUtil msg = Main.getBotManager().getMsgu();
        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        manager.getScheduler().setLooping(!manager.getScheduler().isLooping());
        msg.sendOk(musicChannel, Emoji.REPEAT + " Looping for queue set to `" + manager.getScheduler().isLooping() + "`");
    }

    @Override
    public String name() {
        return "repeat";
    }
    @Override
    public String usage() {
        return "{prefix}:repeat";
    }
    @Override
    public String description() {
        return "Toggle endless mode (loops queue)";
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
        return List.of("r", "endless", "repeatqueue", "loop", "loopmode");
    }
}
