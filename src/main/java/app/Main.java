package app;

import app.controllers.RoutingController;
import app.config.SessionConfig;
import app.config.ThymeleafConfig;
import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import java.io.IOException;


public class Main {

    private static final String USER = System.getenv("user");
    private static final String PASSWORD = System.getenv("password");
    private static final String URL = "jdbc:postgresql://" +  System.getenv("ip") + ":5432/%s?currentSchema=public";
    private static final String DB = "CarportFog";

    public static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args) throws IOException { //har fjernet <Email, Mail> efter "static" midlertidigt grundet sendgrid

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(SessionConfig.sessionConfig()));
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
        }).start(7171);

        RoutingController.startRouting(app, connectionPool);


    }
}
