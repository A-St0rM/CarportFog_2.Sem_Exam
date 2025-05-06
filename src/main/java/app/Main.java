package app;

import app.controllers.RoutingController;
import app.config.SessionConfig;
import app.config.ThymeleafConfig;

import app.persistence.ConnectionPool;

import com.sendgrid.helpers.mail.objects.Personalization;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import java.io.IOException;

import com.sendgrid.SendGrid;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.Method;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

public class Main {

    private static final String USER = "postgres";
    private static final String PASSWORD = "Ahlam1982";
    private static final String URL = "jdbc:postgresql://157.230.98.129:5432/%s?currentSchema=public";
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
