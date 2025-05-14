package app.controllers;

import app.injectorHandler.DependencyInjector;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;

public class RoutingController {

public static void startRouting(Javalin app, ConnectionPool connectionPool) {

    DependencyInjector di = new DependencyInjector(connectionPool);

    OrderController orderController = di.getOrderController();
    AdminController adminController = di.getAdminController();


    //General Routing
    app.get("/", ctx -> ctx.render("index.html"));
    app.get("/details", ctx -> ctx.render("details.html"));
    app.get("/additions", ctx -> ctx.render("additions.html"));

    //Middleware beskytte admin ruter
    app.before("/admin/*", ctx -> {
        // Undtagelse: login- og oprettelsessider
        String path = ctx.path();
        if (!path.equals("/admin/login")) {
            if (ctx.sessionAttribute("currentAdmin") == null) {
                ctx.redirect("/admin/login");
            }
        }
    });

    app.get("/admin/login", ctx -> ctx.render("admin_login.html"));
    app.get("/admin/create", ctx -> ctx.render("create_admin.html"));
    app.get("/admin/dashboard", ctx -> orderController.showAllOrders(ctx));
    app.get("/admin/order/{orderId}/bom", ctx -> orderController.showBOMPage(ctx));
    app.get("/specifications", ctx -> ctx.render("specifications.html"));



    app.post("/details", ctx -> orderController.handleDetailsPost(ctx));
    app.post("/additions", ctx -> orderController.handleAdditionsPost(ctx));
    app.post("/specifications", ctx -> orderController.handleSpecificationsPost(ctx));
    app.post("/admin/login", ctx -> adminController.adminLogin(ctx));
    app.post("/admin/create", ctx -> adminController.createAdmin(ctx));
    app.post("/admin/order/set-total/{orderId}", ctx -> orderController.handleUpdateTotalPrice(ctx));
    app.post("/admin/order/send-payment/{id}", ctx -> orderController.handleSendPaymentEmail(ctx));




}


}