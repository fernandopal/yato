package es.fernandopal.yato.commands.general;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.*;
import java.util.List;

public class StatusCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final List<JDA> shards = Main.getShardManager().getShards();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setDescription(
                "**Global status:**\n" +
                "- AVG ping: " + Main.getShardManager().getAverageGatewayPing() + "\n" +
                /*"- Users: " + Main.getBotManager().getAllUsers().size() + "\n" +*/
                /*"- Guilds: " + Main.getBotManager().getAllGuilds().size() + "\n" +*/
                "\n" +
                "**Per shard status:**"
        );

        Thread thread = new Thread(() -> {
            shards.forEach(shard -> {
                final JDA.ShardInfo shardInfo = shard.getShardInfo();

                long gatewayPing = shard.getGatewayPing();
                if(gatewayPing < 0) { gatewayPing = 0; }

                final String embedTitle = "Shard " + shardInfo.getShardId() + " " + Main.getBotManager().getStatusAsEmoji(shardInfo.getShardId());
                final String embedString =
                        "- WS ping: " + gatewayPing + "ms\n"/* +*/
                                /*"- Users: " + shard.getUsers().size() + "\n" +*/
                                /*"- Guilds: " + shard.getGuilds().size()*/;

                embed.addField(embedTitle, embedString, true);
            });

            ctx.getChannel().sendMessage(embed.build()).queue();
        });
        thread.start();
    }

    @Override public String name() {
        return "status";
    }
    @Override public String usage() {
        return "{prefix}:status";
    }
    @Override public String description() {
        return "Shows the current per shard status";
    }
    @Override public PermLevel permLevel() {
        return PermLevel.GUILD_USER;
    }
    @Override public CommandType commandType() {
        return CommandType.GENERAL;
    }
    @Override public List<String> getAliases() { return List.of("shards"); }
}
