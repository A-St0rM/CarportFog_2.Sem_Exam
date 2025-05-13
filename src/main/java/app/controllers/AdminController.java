package app.controllers;

import app.DTO.AdminDTO;

import app.exceptions.DatabaseException;
import app.persistence.AdminMapper;
import app.persistence.ConnectionPool; // Needed if CustomerMapper is used
import app.persistence.CustomerMapper;
import app.util.PasswordUtil;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import app.persistence.CustomerMapper;
import java.sql.SQLException;


public class AdminController {

    private final AdminMapper adminMapper;
    private final CustomerMapper customerMapper;

    public AdminController(AdminMapper adminMapper, CustomerMapper customerMapper) {
        this.adminMapper = adminMapper;
        this.customerMapper = customerMapper;
    }

    public void adminLogin(@NotNull Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password"); // Plain password

        try {
            // checks the hashing code
            AdminDTO adminDTO = adminMapper.login(email, password);
            // success with login
            ctx.sessionAttribute("currentAdmin", adminDTO); // Use a consistent session key
            // Redirect to the admin dashboard
            ctx.redirect("/admin/dashboard");

        } catch (DatabaseException e) {
            // Handle known login errors (email not found, wrong password)
            ctx.attribute("message", e.getMessage());
            ctx.render("admin_login.html"); //
        } catch (SQLException e) {
            System.err.println("SQL Exception during admin login: " + e.getMessage());
            ctx.attribute("message", "En databasefejl opstod");
            ctx.render("admin_login.html");
        }
    }

    public void createAdmin(@NotNull Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        String passwordConfirm = ctx.formParam("password2");

        if (password == null || password.isEmpty() || !password.equals(passwordConfirm)) {
            ctx.attribute("message", "Kodeordene matcher ikke");
            ctx.render("admin/create_admin.html");
            return;
        }
        String hashedPassword = PasswordUtil.hashPassword(password);
        try {
            adminMapper.createAdmin(email, hashedPassword);
            ctx.attribute("message", "Admin oprettet: " + email);
            // Redirect to dashboard
            ctx.redirect("/admin/dashboard");

        } catch (DatabaseException e) {
            // Handle errors from mapper (e.g., duplicate email)
            ctx.attribute("message", e.getMessage());
            ctx.render("admin/create_admin.html");
        }
    }
}