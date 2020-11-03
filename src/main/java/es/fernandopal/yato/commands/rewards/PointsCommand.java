package es.fernandopal.yato.commands.rewards;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.util.MessageUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class PointsCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel tc = ctx.getChannel();
        final MessageUtil msg = new MessageUtil();
        final Member member = ctx.getMember();

        msg.sendOk(tc, "You have " + Main.getDb().getTotalPoints(member.getIdLong()) + "¥");
    }

    @Override
    public String name() {
        return "points";
    }
    @Override
    public String usage() {
        return "{prefix}:poins";
    }
    @Override
    public String description() {
        return "See how much yens do you have";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override
    public CommandType commandType() {
        return CommandType.REWARDS;
    }
    @Override
    public List<String> getAliases() {
        return List.of("yens", "¥");
    }
}
