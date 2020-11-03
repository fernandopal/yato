package es.fernandopal.yato.commands.music;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.core.audio.AudioManagerController;
import es.fernandopal.yato.core.audio.GuildAudioManager;
import es.fernandopal.yato.core.exceptions.CantJoinVoiceChannelException;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class JoinCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final Guild guild = ctx.getGuild();
        final TextChannel tc = ctx.getChannel();
        final Member member = ctx.getMember();
        final Message message = ctx.getMessage();

        final GuildAudioManager manager = AudioManagerController.getGuildAudioManager(guild);
        final MessageUtil msg = Main.getBotManager().getMsgu();
        TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));

        if(musicChannel == null) { musicChannel = tc; }

        if(member.getVoiceState() == null || !member.getVoiceState().inVoiceChannel()) {
            msg.sendError(musicChannel, Emoji.ERROR + " You're not connected to a voice channel.");
            message.addReaction(Emoji.CROSS).queue();
        } else {
            try {
                manager.openConnection(member.getVoiceState().getChannel(), musicChannel);
            } catch (CantJoinVoiceChannelException ex) {
//                ex.printStackTrace();
                return;
            }
            message.addReaction(Emoji.reactFromCustom(Emoji.CHECK)).queue();
        }
    }

    @Override
    public String name() {
        return "join";
    }
    @Override
    public String usage() {
        return "{prefix}:join";
    }
    @Override
    public String description() {
        return "Move the bot to the channel you're in";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_DJ;
    }
    @Override
    public CommandType commandType() {
        return CommandType.MUSIC;
    }
}
