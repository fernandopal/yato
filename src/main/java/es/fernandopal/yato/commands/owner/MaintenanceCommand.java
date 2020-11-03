package es.fernandopal.yato.commands.owner;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;

import java.util.List;

public class MaintenanceCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        Main.maintenance = !Main.maintenance;

//        String EoD = Main.maintenance ? "enabled" : "disabled";

        ctx.getChannel().sendMessage("Maintenance mode " + (Main.maintenance ? "enabled" : "disabled")).queue();
    }

    @Override
    public String name() {
        return "maintenance";
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
    @Override
    public List<String> getAliases() {
        return List.of("mant", "mantenimiento");
    }
}
