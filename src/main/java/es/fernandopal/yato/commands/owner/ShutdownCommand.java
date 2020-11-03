package es.fernandopal.yato.commands.owner;

import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.ICommand;
import me.duncte123.botcommons.BotCommons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownCommand implements ICommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownCommand.class);

    @Override
    public void handle(CommandContext ctx) {
            LOGGER.info("Shutting down");
            BotCommons.shutdown(ctx.getJDA());
//        } else { ctx.getChannel().sendMessage("Haha good luck with that dude.").queue(); }
    }

    @Override
    public String name() {
        return "shutdown";
    }
    @Override
    public String usage() {
        return "";
    }
    @Override
    public String description() {
        return "";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.BOT_OWNER;
    }
    @Override
    public CommandType commandType() {
        return CommandType.HIDDEN;
    }
}
