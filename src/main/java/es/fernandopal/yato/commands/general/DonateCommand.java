package es.fernandopal.yato.commands.general;

import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.util.MessageUtil;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.List;

public class DonateCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel tc = ctx.getChannel();
        MessageUtil msg = new MessageUtil();

        msg.send(tc, "You can donate at [Patreon](https://www.patreon.com/yatods) or [PayPal](https://www.paypal.me/fernandopm00)\n" +
                "Thank you all for the support <3", Color.BLUE);
    }

    @Override
    public String name() {
        return "donate";
    }
    @Override
    public String usage() {
        return "{prefix}:donate";
    }
    @Override
    public String description() {
        return "If you want to support yato and get rewards use the links on this command";
    }
    @Override
    public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override
    public CommandType commandType() {
        return CommandType.GENERAL;
    }
    @Override
    public List<String> getAliases() {
        return List.of("rewards", "buypremium", "support", "premium");
    }
}
