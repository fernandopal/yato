package es.fernandopal.yato.core.web.routes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.web.Auth;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GetShards {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetShards.class);

    public static void execute(@NotNull RoutingContext context) {
        final HttpServerRequest request = context.request();
        final HttpServerResponse response = context.response();
        try {
            Auth.isAuthenticated(request, response);

            final List<JSONObject> shardsJsonArray = new ArrayList<>();

            Main.getShardManager().getShards().forEach(shard -> {
                long gatewayPing = shard.getGatewayPing();
                if(gatewayPing < 0) { gatewayPing = 0; }

                final int shardId = shard.getShardInfo().getShardId();

                final JSONObject shardJson = new JSONObject()
                        .put("shardId", shardId)
                        .put("status", Main.getBotManager().getStatus(shardId))
                        .put("ws_ping", gatewayPing)
                        .put("users", shard.getUserCache().size())
                        .put("guilds", shard.getGuildCache().size());

                shardsJsonArray.add(shardJson);
            });

            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            response.setStatusCode(200).setStatusMessage("ok").end(gson.toJson(shardsJsonArray));
        } catch (Exception error) {
            response.setStatusCode(500).setStatusMessage(error.getMessage()).end();
        }
    }
}