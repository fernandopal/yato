package es.fernandopal.yato.core.web.routes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.web.Auth;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetGuild {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetGuild.class);

    public static void execute(@NotNull RoutingContext context) {
        final HttpServerRequest request = context.request();
        final HttpServerResponse response = context.response();
        try {
            Auth.isAuthenticated(request, response);

            final String guildId = request.getParam("guild_id");
            if (guildId == null) {
                final JSONObject errJson = new JSONObject().put("error", "guildId_not_included");
                response.setStatusCode(400).setStatusMessage("Please send a valid guild_id").end(errJson.toString());
                return;
            }

            final Guild guild = Main.getGuildById(guildId);
            if (guild == null) {
                final JSONObject errJson = new JSONObject().put("error", "invalid_guildId");
                response.setStatusCode(400).setStatusMessage("The guild_id doesn't belong to any guild").end(errJson.toString());
                return;
            }

            if(!Main.getShardManager().getGuilds().contains(guild)) {
                final JSONObject errJson = new JSONObject().put("error", "bot_not_in_guild");
                response.setStatusCode(400).setStatusMessage("The bot is not on that guild").end(errJson.toString());
                return;
            }

            int bots = (int) guild.getMembers().stream().filter(m -> m.getUser().isBot()).count();

            final JSONObject guildJson = new JSONObject()
                    .put("name", guild.getName())
                    .put("region", guild.getRegion())
                    .put("icon", guild.getIconUrl() == null ? JSONObject.NULL : guild.getIconUrl())
                    .put("members", guild.getMemberCount())
                    .put("bots", bots)
                    .put("channels", guild.getChannels().size())
                    .put("owner", guild.getOwnerId())
                    .put("time_created", guild.getTimeCreated())
                    .put("bot_prefix", Main.getDb().getPrefix(Long.parseLong(guildId)));

            response.setStatusCode(200).setStatusMessage("ok").end(guildJson.toString());
        } catch (Exception error) {
            response.setStatusCode(500).setStatusMessage(error.getMessage()).end();
        }
    }
}