package app.controllers;

import app.injectorHandler.DependencyInjector; // Assuming this exists and works as intended
import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context;

// Removed java.util.Map import as it wasn't used directly here

public class RoutingController {

    // Define route paths as constants (English)
    private static final String ROUTE_START = "/";
    private static final String ROUTE_SPECIFICATIONS = "/specifications"; // Was already English-like
    private static final String ROUTE_ADDITIONS = "/additions";         // Replaced /tilfoejelser
    private static final String ROUTE_DETAILS = "/details";           // Replaced /oplysninger
    // Add more routes as needed (e.g., admin, order confirmation)
    private static final String ROUTE_ADMIN_LOGIN = "/admin/login"; // Added admin route

    public static void startRouting(Javalin app, ConnectionPool connectionPool) {

        // Dependency Injector - assuming correct setup
        // You might need to inject specific controllers/services here later
        // DependencyInjector di = new DependencyInjector(connectionPool);

        // Define routes for each page (using English constants and method references)
        app.get(ROUTE_START, RoutingController::renderStartPage);
        app.get(ROUTE_SPECIFICATIONS, RoutingController::renderSpecificationsPage); // Renamed method
        app.get(ROUTE_ADDITIONS, RoutingController::renderAdditionsPage);       // Renamed method
        app.get(ROUTE_DETAILS, RoutingController::renderDetailsPage);           // Renamed method

        // Placeholder for admin login page route
        app.get(ROUTE_ADMIN_LOGIN, RoutingController::renderAdminLoginPage); // Added admin render method

        // Add POST routes later to handle form submissions, e.g.:
        // app.post(ROUTE_SPECIFICATIONS, ctx -> handleSpecificationsForm(ctx));
    }

    // --- Page Rendering Methods ---

    // Method to render the Start page (Index)
    private static void renderStartPage(Context ctx) {
        // Pass model with current page info (English value)
        ctx.attribute("currentPage", "start"); // Value used for CSS class check
        ctx.render("index.html"); // Template filename
    }

    // Method to render the Specifications page
    private static void renderSpecificationsPage(Context ctx) {
        // TODO: Fetch any necessary data from DB/session here
        ctx.attribute("currentPage", "specifications"); // English value
        ctx.render("specifications.html"); // English template filename
    }

    // Method to render the Additions page
    private static void renderAdditionsPage(Context ctx) {
        // TODO: Fetch any necessary data
        ctx.attribute("currentPage", "additions"); // English value
        ctx.render("additions.html"); // English template filename
    }

    // Method to render the Details/Information page
    private static void renderDetailsPage(Context ctx) {
        // TODO: Fetch any necessary data
        ctx.attribute("currentPage", "details"); // English value
        ctx.render("details.html"); // English template filename
    }

    // Method to render the Admin Login page (placeholder)
    private static void renderAdminLoginPage(Context ctx) {
        // TODO: Implement admin login page rendering
        ctx.attribute("currentPage", "adminLogin"); // Or null if no sidebar needed
        ctx.render("admin_login.html"); // Assumes an admin_login.html template
    }

    // --- Form Handling Example (Implement later) ---
    /*
    private static void handleSpecificationsForm(Context ctx) {
        // Get data using ctx.formParam("...")
        // Validate data
        // Save data (session? database?)
        // Redirect to the next step
        ctx.redirect(ROUTE_ADDITIONS); // Use English route constant
    }
    */
}