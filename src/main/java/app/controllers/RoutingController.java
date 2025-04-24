package app.controllers;

import app.injectorHandler.DependencyInjector;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;

public class RoutingController {

    public static void startRouting(Javalin app, ConnectionPool connectionPool) {

        DependencyInjector di = new DependencyInjector(connectionPool);

        app.get("/", ctx -> ctx.render("index.html"));

    }
}
