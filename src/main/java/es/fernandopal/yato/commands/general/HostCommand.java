package es.fernandopal.yato.commands.general;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.List;

public class HostCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        double totalMemory = Main.getRuntime().totalMemory();
        double freeMemory = Main.getRuntime().freeMemory();
        double systemTotal = Main.getSystem().getTotalPhysicalMemorySize();
        double cpu_usage = Math.round(Main.getSystem().getSystemCpuLoad() * 100);
        String system_used = convert(systemTotal - Main.getSystem().getFreePhysicalMemorySize());
        String system_total = convert(systemTotal);
        String runtime_max_alloc = convert((double) Main.getRuntime().maxMemory());
        String runtime_alloc = convert(totalMemory);
        String runtime_free = convert(freeMemory);
        String used_memory = convert(totalMemory - freeMemory);
        String uptime = Main.getBotManager().getDurationBreakdown(Main.getRuntime_mx().getUptime(), false);
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.BLUE)
                .setDescription(
                        "```json\n" +
                                "Process mem used     -> " + used_memory + "\n" +
                                "Process mem free     -> " + runtime_free + "\n" +
                                "Process mem reserved -> " + runtime_alloc + "\n" +
                                "Maximum process mem  -> " + runtime_max_alloc + "\n" +
                                "CPU usage            -> " + cpu_usage + " %" + "\n" +
                                "RAM usage            -> " + system_used + " / " + system_total + "\n" +
                                "```"
                )
                .setFooter("Online for: " + uptime, ctx.getGuild().getSelfMember().getUser().getAvatarUrl())
                .build();

        channel.sendMessage(embed).queue();
    }

    private String convert(Double value) {
        String parsed;
        if (value < 1000000000) {
            double data = Math.round(value / 1048576);
            parsed = String.format("%s MB", data);
            return parsed;
        }
        double data = Math.round(value / 1073741824);
        parsed = String.format("%s GB", data);
        return parsed;
    }

    @Override
    public String name() {
        return "host";
    }
    @Override
    public String usage() {
        return "{prefix}:host";
    }
    @Override
    public String description() {
        return "Shows info about the bot hosting";
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
        return List.of("host", "hostinfo");
    }
}
