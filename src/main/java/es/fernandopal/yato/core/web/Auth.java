package es.fernandopal.yato.core.web;

import es.fernandopal.yato.files.Webserver;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Auth {
    private static final Logger LOGGER = LoggerFactory.getLogger(Auth.class);
    private static final Webserver ws = new Webserver();

    public static void isAuthenticated(@NotNull HttpServerRequest request, HttpServerResponse response) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.equals(ws.getString("auth-token"))) {
            response.setStatusCode(401).setStatusMessage("Unauthorized").end();
            if (auth == null) {
                LOGGER.warn("Rejected " + request.method() + " request to " + request.uri() + " from " + request.host() + " | No Auth Header");
            } else {
                LOGGER.warn("Rejected " + request.method() + " request to " + request.uri() + " from " + request.host() + " | Wrong Rest Auth, Received: " + auth);
            }
            return;
        }
        LOGGER.info("Accepted " + request.method() + " request to " + request.uri() + " from " + request.host());
    }
}
