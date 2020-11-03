package es.fernandopal.yato.commands.general;

import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import net.dv8tion.jda.api.JDA;

public class PingCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        JDA jda = ctx.getJDA();

        jda.getRestPing().queue(
                (ping) ->  {
                    long gatewayPing = jda.getGatewayPing();
                    if(gatewayPing < 0) { gatewayPing = 0; }

                    ctx.getChannel().sendMessageFormat("Response in: %sms\nWS response in: %sms", ping, gatewayPing).queue();
                }
        );
    }

    @Override public String name() {
        return "ping";
    }
    @Override public String usage() {
        return "{prefix}:ping";
    }
    @Override public String description() {
        return "Shows the current respond time of the bot to discord servers";
    }
    @Override public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override public CommandType commandType() {
        return CommandType.GENERAL;
    }
}
