package es.fernandopal.yato.core.web.routes;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.web.Auth;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class GetUser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetUser.class);

    public static void execute(@NotNull RoutingContext context) {
        final HttpServerRequest request = context.request();
        final HttpServerResponse response = context.response();
        try {
            Auth.isAuthenticated(request, response);

            final String userId = request.getParam("user_id");
            if (userId == null) {
                final JSONObject errJson = new JSONObject().put("error", "userId_not_included");
                response.setStatusCode(400).setStatusMessage("Please send a valid user_id").end(errJson.toString());
                return;
            }

            final User user = Main.getUserById(userId);
            if (user == null) {
                final JSONObject errJson = new JSONObject().put("error", "invalid_userId");
                response.setStatusCode(400).setStatusMessage("The user_id doesn't belong to any user").end(errJson.toString());
                return;
            }

            final ArrayList<String> mutualGuilds = new ArrayList<>();
            user.getMutualGuilds().forEach(g -> { mutualGuilds.add(g.getId()); });

//            Main.getShardManager().getGuilds().forEach(System.out::println);

            final JSONObject userJson = new JSONObject()
                    .put("name", user.getName())
                    .put("discriminator", user.getDiscriminator())
                    .put("id", user.getId())
                    .put("asTag", user.getAsTag())
                    .put("avatar", user.getAvatarUrl() == null ? JSONObject.NULL : user.getAvatarUrl())
                    .put("mutualGuilds", new JSONArray(mutualGuilds))
                    .put("bot", user.isBot());

            response.setStatusCode(200).setStatusMessage("ok").end(userJson.toString());
        } catch (Exception error) {
            response.setStatusCode(500).setStatusMessage(error.getMessage()).end();
            error.printStackTrace();
        }
    }
}