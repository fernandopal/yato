package es.fernandopal.yato.commands;

import es.fernandopal.yato.Main;

import es.fernandopal.yato.commands.config.*;
import es.fernandopal.yato.commands.music.*;
import es.fernandopal.yato.commands.rewards.*;
import es.fernandopal.yato.commands.owner.*;
import es.fernandopal.yato.commands.general.*;

import es.fernandopal.yato.listeners.CommandListener;
import es.fernandopal.yato.util.Emoji;
import es.fernandopal.yato.util.MessageUtil;
import es.fernandopal.yato.files.Config;
import lavalink.client.io.LavalinkSocket;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);
    private final List<ICommand> commands = new ArrayList<>();
    private final Config config = new Config();

    public CommandManager() {
        LOGGER.info("Registering commands...");
        addCommand(new HelpCommand(this));
        addCommand(new PingCommand());
        addCommand(new StatusCommand());
        addCommand(new BotCommand());
        addCommand(new DonateCommand());
        addCommand(new HostCommand());

        addCommand(new PointsCommand());
//        addCommand(new VoteCommand());

        addCommand(new JoinCommand());
        addCommand(new NowPlayingCommand());
        addCommand(new PauseCommand());
        addCommand(new PlayCommand());
//        addCommand(new RadioCommand());
        addCommand(new RandomCommand());
        addCommand(new QueueCommand());
        addCommand(new RepeatCommand());
        addCommand(new ShuffleCommand());
        addCommand(new SkipCommand());
        addCommand(new StopCommand());
        addCommand(new ClearCommand());
        addCommand(new VolumeCommand());
//        addCommand(new RemoveCommand());
//        addCommand(new RemoveDupesCommand());
//        addCommand(new PlaylistCommand());
//        addCommand(new SongOfTheDayCommand());

        addCommand(new ShutdownCommand());
        addCommand(new MaintenanceCommand());
//        addCommand(new TestCommand());
//        addCommand(new UploadSongs());

        addCommand(new SetPrefixCommand());
        addCommand(new ToggleQueueMessages());
        addCommand(new ToggleSearchResults());

    }

    private void addCommand(ICommand cmd) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.name().equalsIgnoreCase(cmd.name()));

        if (nameFound) {
            throw new IllegalArgumentException("A command with this name is already present");

        }

        commands.add(cmd);

    }

    public List<ICommand> getCommands() {
        return commands;
    }

    @Nullable
    public ICommand getCommand(@NotNull String search) {
        String searchLower = search.toLowerCase();

        for (ICommand cmd : this.commands) {
            if (cmd.name().equals(searchLower) || cmd.getAliases().contains(searchLower)) {
                return cmd;

            }

        }

        return null;

    }

    public void handle(GuildMessageReceivedEvent event, String prefix) {
        try {
            String[] split = event.getMessage().getContentRaw()
                    .replaceFirst("(?i)" + Pattern.quote(prefix), "") //take off the prefix
                    .split("\\s+"); //Split by whitespaces

            String invoke = split[0].toLowerCase();
            ICommand cmd = this.getCommand(invoke);

            if (cmd != null) {
                final List<String> args = Arrays.asList(split).subList(1, split.length);
                final Member member = event.getMember();
                final Guild guild = event.getGuild();
                final TextChannel channel = event.getChannel();
                channel.sendTyping().queue();

                final CommandContext ctx = new CommandContext(event, args);

                int permLevel = 1;
                if (!guild.getRolesByName("DJ", true).isEmpty()) {
                    if (guild.getRoles().contains(guild.getRolesByName("DJ", true).get(0))) {
                        permLevel = 2;

                    }

                }

                assert member != null;
                if (member.hasPermission(Permission.ADMINISTRATOR)) {
                    permLevel = 3;

                }
                if (member.isOwner()) {
                    permLevel = 4;

                }
                if (config.getList("bot-owners").contains(member.getId())) {
                    permLevel = 5;

                }

                final MessageUtil msg = Main.getBotManager().getMsgu();

                if(cmd.commandType().equals(CommandType.MUSIC)) {
                    int availableNodes = 0;
                    for(LavalinkSocket n : Main.getBotManager().getLavalink().getNodes()) { if(n.isAvailable()) { availableNodes++; } }
                    if(availableNodes == 0) {
                        msg.sendError(channel, "Music commands are temporally disabled because all our nodes are offline.");
                        return;

                    }

                }

                if (cmd.permLevel().equals(PermLevel.BOT_OWNER) && permLevel < 5) {
                    msg.sendError(channel, Emoji.ERROR + " Haha good luck with that dude.");

                } else if (cmd.permLevel().equals(PermLevel.BOT_OWNER) && permLevel < 4) {
                    msg.sendError(channel, Emoji.ERROR + " Only the server owner can use this command.");

                } else if (cmd.permLevel().equals(PermLevel.GUILD_ADMIN) && permLevel < 3) {
                    msg.sendError(channel, Emoji.ERROR + " Only server admins and owners can use this command.\nThe bot recognise admins as the people with the permission `" + Permission.ADMINISTRATOR + "`");

                } else if (cmd.permLevel().equals(PermLevel.GUILD_DJ) && permLevel < 2) {
                    msg.sendError(channel, Emoji.ERROR + " This command is only for DJ's.\nThe bot recognise DJ's as the people with a role called DJ");

                } else {
                    cmd.handle(ctx);
                    LOGGER.info("Command '{}' executed in '{}' by '{}'", event.getMessage().getContentRaw(), event.getGuild().toString(), event.getAuthor().toString());

                }
            } else {
                event.getChannel().sendMessage(":warning: Command not found").queue();

            }
        } catch (Exception e) {
            Invite invite = null;
            try {
                List<Invite> i = event.getGuild().retrieveInvites().complete();

                if(i.isEmpty()) { event.getChannel().createInvite().setTemporary(false).setMaxUses(1).queue(); }

                invite = i.get(0);

            } catch (Exception ignored) { }

            LOGGER.error(
                    "Can't process a command:\n" +
                    "-> " + e.getMessage() + "\n" +
                    "-> " + Arrays.toString(e.getStackTrace()) + "\n" +
                    "-> " + event.getGuild() + "\n" +
                    "-> " + event.getChannel() + "\n" +
                    "-> " + invite

            );

        }

    }

}