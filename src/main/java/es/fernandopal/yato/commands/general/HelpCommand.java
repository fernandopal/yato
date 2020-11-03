package es.fernandopal.yato.commands.general;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.*;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.List;

public class HelpCommand implements ICommand {
    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();
        final User user = ctx.getAuthor();
        final MessageUtil msg = Main.getBotManager().getMsgu();

        if(args.isEmpty()) {
            final StringBuilder csb = new StringBuilder();

            try {
                PrivateChannel pc = user.openPrivateChannel().complete();

                csb.append(Emoji.CLIPBOARD).append(" __**COMMAND LIST**__ ").append(Emoji.CLIPBOARD).append(" \n")
                    .append("**Permission levels:**\n")
                    .append(Emoji.S_RED_TRIANGLE).append(" - Commands only for server owner/admins\n")
                    .append(Emoji.S_ORANGE_DIAMOND).append(" - Commands only for server DJ's\n")
                    .append(Emoji.S_BLUE_DIAMOND).append(" - Commands for everyone\n");

                csb.append("\n");
                csb.append("**").append(CommandType.CONFIG).append("**\n");
                for(int i = 0; i < manager.getCommands().size(); i++) {
                    final ICommand cmd = manager.getCommands().get(i);

                    if(cmd.commandType().equals(CommandType.CONFIG)) {
                        csb.append(getPermEmoji(cmd.permLevel())).append("  **").append(cmd.name()).append("** - `").append(cmd.description()).append("`\n");
                    }
                }

                csb.append("\n");
                csb.append("**").append(CommandType.GENERAL).append("**\n");
                for(int i = 0; i < manager.getCommands().size(); i++) {
                    final ICommand cmd = manager.getCommands().get(i);

                    if(cmd.commandType().equals(CommandType.GENERAL)) {
                        csb.append(getPermEmoji(cmd.permLevel())).append("  **").append(cmd.name()).append("** - `").append(cmd.description()).append("`\n");
                    }
                }

                csb.append("\n");
                csb.append("**").append(CommandType.REWARDS).append("**\n");
                for(int i = 0; i < manager.getCommands().size(); i++) {
                    final ICommand cmd = manager.getCommands().get(i);

                    if(cmd.commandType().equals(CommandType.REWARDS)) {
                        csb.append(getPermEmoji(cmd.permLevel())).append("  **").append(cmd.name()).append("** - `").append(cmd.description()).append("`\n");
                    }
                }

                csb.append("\n");
                csb.append("**").append(CommandType.MUSIC).append("**\n");
                for(int i = 0; i < manager.getCommands().size(); i++) {
                    final ICommand cmd = manager.getCommands().get(i);

                    if(cmd.commandType().equals(CommandType.MUSIC)) {
                        csb.append(getPermEmoji(cmd.permLevel())).append("  **").append(cmd.name()).append("** - `").append(cmd.description()).append("`\n");
                    }
                }

                msg.sendPm(pc, csb.toString(), Color.ORANGE);
                msg.sendOk(channel, user.getAsMention() + " I've sent you the help to your dm");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        String search = args.get(0);
        ICommand command = manager.getCommand(search);

        if(command == null) {
            channel.sendMessage("Nothing found for " + search).queue();
            return;
        }

        msg.sendOk(channel, "Usage: `" + command.usage().replace("{prefix}", Main.getPrefix(channel.getGuild().getIdLong())) + "`");
    }

    private String getPermEmoji(PermLevel pl) {
        switch (pl) {
            case GUILD_OWNER:
            case GUILD_ADMIN:
                return Emoji.S_RED_TRIANGLE;
            case GUILD_DJ: return Emoji.S_ORANGE_DIAMOND;
            case GUILD_USER:
            default: return Emoji.S_BLUE_DIAMOND;
        }
    }

    @Override
    public String name() {
        return "help";
    }
    @Override
    public String usage() {
        return "{prefix}:help <command>";
    }
    @Override
    public String description() {
        return "Shows a list with the commands of the bot";
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
        return List.of("commands", "cmds", "h", "commandlist");
    }
}
