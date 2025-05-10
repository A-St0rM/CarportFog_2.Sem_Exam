package app.controllers;

import app.injectorHandler.DependencyInjector;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;

public class RoutingController {

public static void startRouting(Javalin app, ConnectionPool connectionPool) {

    DependencyInjector di = new DependencyInjector(connectionPool);

    OrderController orderController = di.getOrderController();


    //General Routing
    app.get("/", ctx -> ctx.render("index.html"));
    app.get("/details", ctx -> ctx.render("details.html"));
    app.get("/additions", ctx -> ctx.render("additions.html"));
    app.get("/specifications", ctx -> ctx.render("specifications.html"));


    app.post("/details", ctx -> orderController.handleDetailsPost(ctx));

}


}