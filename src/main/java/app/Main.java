package app;

import app.controllers.AdminController;
import app.controllers.RoutingController;
import app.config.SessionConfig;
import app.config.ThymeleafConfig;
import app.persistence.AdminMapper;
import app.persistence.ConnectionPool;
import app.persistence.CustomerMapper;
import app.util.PasswordUtil;
import com.sendgrid.helpers.mail.objects.Personalization;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import java.io.IOException;


public class Main {

    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=public";
    private static final String DB = "CarportFog"; //

    public static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args) throws IOException {

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.jetty.modifyServletContextHandler(handler -> handler.setSessionHandler(SessionConfig.sessionConfig()));
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
        }).start(7171);

        AdminMapper adminMapper = new AdminMapper(connectionPool);
        CustomerMapper customerMapper = new CustomerMapper(connectionPool);
        AdminController adminController = new AdminController(adminMapper, customerMapper);
        RoutingController.startRouting(app, connectionPool, adminController);


        /* funktionalitet af admin og password hashing der skal implementeres
        String userEmail = "admin@test.dk";
        String userPassword = "password123";
        String hashedPassword = PasswordUtil.hashPassword(userPassword);
        System.out.println("Password hashed to: " + hashedPassword);
        String storedHashForUser = hashedPassword;
        System.out.println("--> Hashed password stored for user " + userEmail);

        String loginEmailAttempt1 = "admin@test.dk";
        String loginPasswordAttempt1 = "password123";
        System.out.println("Simulating login attempt for: " + loginEmailAttempt1 + " with password: " + loginPasswordAttempt1);
        String retrievedHash = storedHashForUser;
        boolean loginSuccess = PasswordUtil.checkPassword(loginPasswordAttempt1, retrievedHash);
        System.out.println("Login successful? " + loginSuccess);
       */
    }

}