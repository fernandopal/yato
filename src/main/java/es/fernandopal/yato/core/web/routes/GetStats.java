package es.fernandopal.yato.core.web.routes;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.web.Auth;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetStats {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetStats.class);

    public static void execute(@NotNull RoutingContext context) {
        final HttpServerRequest request = context.request();
        final HttpServerResponse response = context.response();
        try {
            Auth.isAuthenticated(request, response);

            final JsonObject stats = new JsonObject()
                    .put("guilds", Main.getShardManager().getGuilds().size())
                    .put("users", Main.getShardManager().getUsers().size())
                    .put("shards", Main.getShardManager().getShardCache().size())
                    .put("avg_shard_ping", (int) Main.getShardManager().getAverageGatewayPing())
                    .put("text_channels", Main.getShardManager().getTextChannelCache().size())
                    .put("voice_channels", Main.getShardManager().getVoiceChannelCache().size())
                    .put("songs_played_today", 0)
                    .put("songs_played_total", 0);

            response.setStatusCode(200).setStatusMessage("ok").end(stats.toString());
        } catch (Exception error) {
            response.setStatusCode(500).setStatusMessage(error.getMessage()).end();
        }
    }
}