package es.fernandopal.yato.commands.config;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class ToggleSearchResults implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final Member member = ctx.getMember();
        final Guild guild = ctx.getGuild();

        if(!member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessage("You don't have permissions to use this command").queue();
            return;
        }

        Main.getDb().toggleSearchResults(guild.getIdLong());

        if(Main.getDb().hasSearchResultsEnabled(guild.getIdLong())) {
            channel.sendMessageFormat("The bot will now display the first 5 results of a youtube search to let you choose one").queue();
        } else {
            channel.sendMessageFormat("The bot will now play the first result of youtube when using `y:p <search term>`").queue();
        }
    }

    @Override
    public String name() {
        return "searchresults";
    }
    @Override
    public String usage() {
        return "{prefix}:searchresults <prefix>";
    }
    @Override
    public String description() {
        return "Toggle the search results selector of 'y:p <search term>' on/off (default: on)";
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
        return List.of("togglesearchresults", "tsr");
    }
}
