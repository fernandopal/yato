package es.fernandopal.yato.core.web;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.web.routes.GetGuild;
import es.fernandopal.yato.core.web.routes.GetShards;
import es.fernandopal.yato.core.web.routes.GetStats;
import es.fernandopal.yato.core.web.routes.GetUser;
import es.fernandopal.yato.files.Webserver;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YatoRestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(YatoRestServer.class);

    private final Webserver ws = new Webserver(); //WebServer config file

    private final Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(Runtime.getRuntime().availableProcessors()));
    private final HttpServer server = vertx.createHttpServer();
    private final Router router = Router.router(vertx);
    private final Router apiRouter = Router.router(vertx);

    //Listen function for the rest api, this declares the routes void and starts the rest api on the specified port
    public void listen() {
        if(ws.getInt("port") == null || ws.getString("auth-token") == null) {
            LOGGER.warn("Something is wrong with your webserver.json, check if you have configured it properly. Yato api disabled.");
            return;

        }

        LOGGER.info("Starting to listen for rest requests...");
//        apiRouter.route().handler(BodyHandler.create());
        apiRouter.route(HttpMethod.GET, "/stats")
                .produces("application/json")
                .blockingHandler(GetStats::execute, true)
                .enable();
        apiRouter.route(HttpMethod.GET, "/shards")
                .produces("application/json")
                .blockingHandler(GetShards::execute, true)
                .enable();
        apiRouter.route(HttpMethod.GET, "/guild")
                .produces("application/json")
                .blockingHandler(GetGuild::execute, true)
                .enable();
        apiRouter.route(HttpMethod.GET, "/user")
                .produces("application/json")
                .blockingHandler(GetUser::execute, true)
                .enable();
        apiRouter.route("/*")
                .handler(StaticHandler.create().setIndexPage("yato.html"))
                .enable();
        router.mountSubRouter("/api/", apiRouter);
        router.route("/*")
                .handler(StaticHandler.create().setIndexPage("yato.html"))
                .enable();
        LOGGER.info("API route handlers are now set!");

        //

        int port = ws.getInt("port");

        server.requestHandler(router).listen(port);
        LOGGER.info("Success. Yato api is now online and ready! Configured to listen at http://localhost:" + port);

    }

}
