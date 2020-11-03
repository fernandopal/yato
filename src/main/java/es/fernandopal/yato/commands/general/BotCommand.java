package es.fernandopal.yato.commands.general;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.commands.CommandContext;
import es.fernandopal.yato.commands.CommandType;
import es.fernandopal.yato.commands.ICommand;
import es.fernandopal.yato.commands.PermLevel;
import es.fernandopal.yato.files.Config;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class BotCommand implements ICommand {
    private final Config config = new Config();

    @Override
    public void handle(CommandContext ctx) {
        ctx.getChannel().sendMessage(new EmbedBuilder()
                .setColor(Color.GRAY)
                .setThumbnail(Objects.requireNonNull(ctx.getJDA().getUserById(config.getString("bot-id"))).getAvatarUrl())
                .setDescription(":robot: **Yato | 夜ト**")
                .addField("Main open source libraries used:",
                        "**-**  [JDA](https://github.com/DV8FromTheWorld/JDA)\n" +
                                "**-**  [lavalink](https://github.com/Frederikam/Lavalink)\n" +
                                "**-**  [lavalink-client](https://github.com/FredBoat/Lavalink-Client)", false)
                .addField("Bugs? Problems? Requests?",
                        "If you find a bug or you want to request a new feature:\n" +
                                "**-  [Discord](" + Main.getDb().getBotServerInvite() + ")** || **[Website](" + Main.getDb().getBotWebsiteUrl() + ")**\n" +
                                "**-  E-Mail:** `mail@fernandopal.es`\n" +
                                "**-  Add me: `fernandopal#8931`**", false)
                .addField("Invite me to your server", "[Yato | 夜ト](https://discord.com/oauth2/authorize?client_id=454272495114256394&permissions=8&scope=bot)", false)
                .addField("Donations or tips",
                        "**-  [Patreon](https://patreon.com/yatods)**\n" +
                                "**-  [PayPal](https://www.paypal.me/fernandopm00)**\n", false)
                .addField("Bot stats",
                  /*"Text Channels: " + Main.getShardManager().getTextChannelCache().size() + "\n" +*/
                        /*"Voice Channels: " + Main.getShardManager().getVoiceChannelCache().size() + "\n" +*/
//                        "Active Players: " + Main.getBotManager().getLavalink().getLinks().size() + "\n" +
                        /*"Users: " + Main.getShardManager().getUserCache().size() + "\n" +*/
                        /*"Guilds: " + Main.getShardManager().getGuildCache().size() + "\n" +*/
                        "Shards: " + Main.getShardManager().getShardCache().size() + " (AVG ping: " + Main.getShardManager().getAverageGatewayPing() + "ms)", false)
                .build()
        ).queue();
    }

    @Override
    public String name() {
        return "bot";
    }
    @Override
    public String usage() {
        return "{prefix}:bot";
    }
    @Override
    public String description() {
        return "Shows info about the bot";
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
        return List.of("info", "botinfo", "stats", "status");
    }
}
