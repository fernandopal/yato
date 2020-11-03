package es.fernandopal.yato.commands;

import java.util.List;

public interface ICommand {
    void handle(CommandContext ctx);
    String name();
    String usage();
    String description();
    PermLevel permLevel();
    CommandType commandType();
    default List<String> getAliases() {
        return List.of();
    }
}
