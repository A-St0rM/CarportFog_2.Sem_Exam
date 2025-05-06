package app.controllers;

import app.DTO.AdminDTO;
// import app.DTO.CustomerDTO; // Assuming not needed here
// import app.entities.Admin; // Assuming not needed here
// import app.entities.Customer; // Assuming not needed here
import app.exceptions.DatabaseException;
import app.persistence.AdminMapper;
import app.persistence.ConnectionPool; // Needed if CustomerMapper is used
import app.persistence.CustomerMapper; // Assuming needed for constructor
import app.util.PasswordUtil; // <<< IMPORT PASSWORD UTIL
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
// import java.util.List; // Assuming not needed here

public class AdminController {

    private final AdminMapper adminMapper;
    private final CustomerMapper customerMapper; // Assuming you need this

    // Constructor injection
    public AdminController(AdminMapper adminMapper, CustomerMapper customerMapper) {
        this.adminMapper = adminMapper;
        this.customerMapper = customerMapper;
    }

    public void adminLogin(@NotNull Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password"); // Plain text from form

        try {
            // Call the updated login method - it handles the hashing check
            AdminDTO adminDTO = adminMapper.login(email, password);

            // Login successful - store user info in session
            ctx.sessionAttribute("currentAdmin", adminDTO); // Use a consistent session key
            System.out.println("Admin login successful for: " + adminDTO.getEmail()); // Log success

            // Redirect to the admin dashboard
            ctx.redirect("/admin/dashboard");

        } catch (DatabaseException e) {
            // Handle known login errors (email not found, wrong password)
            System.out.println("Admin login failed: " + e.getMessage());
            ctx.attribute("message", e.getMessage()); // Pass the specific error message
            ctx.render("admin_login.html"); // Re-render login page with error
        } catch (SQLException e) {
            System.err.println("SQL Exception during admin login: " + e.getMessage());
            ctx.attribute("message", "En databasefejl opstod. Prøv igen senere.");
            ctx.render("admin_login.html"); // Re-render login page
        }
    }

    public void createAdmin(@NotNull Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password"); // Read first password field
        String passwordConfirm = ctx.formParam("password2"); // Read confirmation field

        // 1. Check if passwords are provided and match
        if (password == null || password.isEmpty() || !password.equals(passwordConfirm)) {
            ctx.attribute("message", "Kodeordene matcher ikke");
            ctx.render("admin/create_admin.html"); // Adjust path to your template if needed
            return; // Stop if passwords don't match
        }

        // 2. Hash the password using PasswordUtil
        String hashedPassword = PasswordUtil.hashPassword(password);

        // 3. Try to create the admin in the database with the HASHED password
        try {
            adminMapper.createAdmin(email, hashedPassword); // Call mapper with hashed password
            System.out.println("Admin created successfully for: " + email);
            ctx.attribute("message", "Admin oprettet: " + email); // Success message
            // Redirect to dashboard or maybe back to a user list?
            ctx.redirect("/admin/dashboard"); // Example redirect

        } catch (DatabaseException e) {
            // Handle errors from mapper (e.g., duplicate email)
            System.err.println("Failed to create admin: " + e.getMessage());
            ctx.attribute("message", e.getMessage()); // Show specific error from mapper
            ctx.render("admin/create_admin.html"); // Re-render creation page with error
        }
    }

    // Other admin controller methods...
}