package es.fernandopal.yato.commands.config;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.ydba.StaticHolder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class SetPrefixCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final List<String> args = ctx.getArgs();
        final Member member = ctx.getMember();

        if(!member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessage("You don't have permissions to use this command").queue();
            return;
        }

        if(args.isEmpty()) {
            channel.sendMessage("You need to specify a prefix").queue();
            return;
        }

        final String newPrefix = String.join("", args);
        updatePrefix(ctx.getGuild().getIdLong(), newPrefix);

        channel.sendMessageFormat("New prefix has been set to `%s`", newPrefix).queue();
    }

    @Override
    public String name() {
        return "setprefix";
    }
    @Override
    public String usage() {
        return "{prefix}:setprefix <prefix>";
    }
    @Override
    public String description() {
        return "Sets the prefix for the server";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_ADMIN;
    }
    @Override
    public CommandType commandType() {
        return CommandType.CONFIG;
    }
    @Override
    public List<String> getAliases() {
        return List.of("prefix");
    }

    private void updatePrefix(long guildId, String newPrefix) {
        StaticHolder.PREFIXES.put(guildId, newPrefix);
        Main.getDb().setPrefix(guildId, newPrefix);
    }
}
