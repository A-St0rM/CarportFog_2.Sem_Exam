package app.controllers;

import app.persistence.AdminMapper;   // Assuming you might need this if passing AdminController
import app.persistence.ConnectionPool;
import app.persistence.CustomerMapper; // Assuming you might need this if passing AdminController
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
// import app.entities.Order; // Example: Import your Order class/DTO when ready

public class RoutingController {

    // Define route paths as constants
    private static final String ROUTE_START = "/";
    private static final String ROUTE_SPECIFICATIONS = "/specifications";
    private static final String ROUTE_ADDITIONS = "/additions";
    private static final String ROUTE_DETAILS = "/details";
    private static final String ROUTE_CONFIRMATION = "/confirmation";
    private static final String ROUTE_PAYMENT = "/payment";
    private static final String ROUTE_PAYMENT_CONFIRMATION = "/payment-confirmation";
    private static final String ROUTE_ADMIN_LOGIN = "/admin/login";
    private static final String ROUTE_ADMIN_DASHBOARD = "/admin/dashboard";
    private static final String ROUTE_ADMIN_OPTIONS = "/admin/options";
    private static final String ROUTE_ADMIN_CREATE = "/admin/create";

    public static void startRouting(Javalin app, ConnectionPool connectionPool, AdminController adminController) {

        // --- GET Routes (for showing pages) ---
        app.get(ROUTE_START, RoutingController::renderStartPage);
        app.get(ROUTE_SPECIFICATIONS, ctx -> renderSpecificationsPage(ctx, connectionPool));
        app.get(ROUTE_ADDITIONS, ctx -> renderAdditionsPage(ctx, connectionPool));
        app.get(ROUTE_DETAILS, ctx -> OrderController.showSvg(ctx, connectionPool));
        app.get(ROUTE_CONFIRMATION, RoutingController::renderConfirmationPage);
        app.get(ROUTE_PAYMENT, ctx -> renderPaymentPage(ctx, connectionPool));
        app.get(ROUTE_PAYMENT_CONFIRMATION, RoutingController::renderPaymentConfirmationPage);
        app.get(ROUTE_ADMIN_LOGIN, RoutingController::renderAdminLoginPage);
        app.get(ROUTE_ADMIN_DASHBOARD, ctx -> renderAdminDashboard(ctx, connectionPool));
        app.get(ROUTE_ADMIN_OPTIONS, ctx -> renderAdminOptionsPage(ctx, connectionPool));
        app.get(ROUTE_ADMIN_CREATE, RoutingController::renderCreateAdminPage);


        // --- POST Routes (for handling form submissions) ---
        app.post(ROUTE_SPECIFICATIONS, ctx -> handleSpecificationsForm(ctx));
        app.post(ROUTE_ADDITIONS, ctx -> handleAdditionsForm(ctx));
        app.post(ROUTE_DETAILS, ctx -> handleDetailsForm(ctx, connectionPool));
        app.post(ROUTE_ADMIN_LOGIN, adminController::adminLogin); // Uses AdminController instance
        app.post(ROUTE_ADMIN_CREATE, adminController::createAdmin); // Uses AdminController instance
        // app.post("/process-payment", ...); // Removed as per your request

        // TODO: app.post(ROUTE_ADMIN_OPTIONS, ctx -> handleAdminOptionsUpdate(ctx, connectionPool));

        // --- Exception Handling ---
        app.exception(ValidationException.class, (e, ctx) -> {
            System.err.println("Validation Error occurred: " + e.getErrors());
            ctx.attribute("error", "Udfyld venligst alle påkrævede felter korrekt.");
            // Consider redirecting back to the form page with error messages
            // For now, a generic error response.
            ctx.status(400).result("Validation Error: Please check your input. " + e.getErrors().toString());
        });
        app.exception(Exception.class, (e, ctx) -> {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Log stack trace during development
            ctx.status(500).result("Internal server error.");
        });
    }

    // --- Page Rendering Methods (All written out fully) ---

    private static void renderStartPage(Context ctx) {
        ctx.attribute("currentPage", "start");
        ctx.render("index.html");
    }

    private static void renderSpecificationsPage(Context ctx, ConnectionPool connectionPool) {
        // TODO: Fetch saved values from session if needed
        ctx.attribute("currentPage", "specifications");
        ctx.render("specifications.html");
    }

    private static void renderAdditionsPage(Context ctx, ConnectionPool connectionPool) {
        // TODO: Fetch saved values from session if needed
        ctx.attribute("currentPage", "additions");
        ctx.render("additions.html");
    }

    private static void renderDetailsPage(Context ctx, ConnectionPool connectionPool) {
        String error = ctx.queryParam("error"); // Check for error from redirect
        ctx.attribute("error", error);
        // TODO: Fetch saved values from session if needed
        ctx.attribute("currentPage", "details");
        ctx.render("details.html");
    }

    private static void renderConfirmationPage(Context ctx) {
        // TODO: Fetch order details from session/DB to display on confirmation
        ctx.attribute("currentPage", "confirmation");
        ctx.render("confirmation.html");
    }

    private static void renderPaymentPage(Context ctx, ConnectionPool connectionPool) {
        // TODO: Fetch necessary order details from session or DB to display name/price
        ctx.attribute("customerName", "Test Kunde (Placeholder)");
        ctx.attribute("totalPrice", 28394.00);
        ctx.attribute("orderId", 123); // Placeholder
        ctx.attribute("pageSubtitle", "Betaling");
        ctx.attribute("currentPage", "payment");
        ctx.render("payment.html");
    }

    private static void renderPaymentConfirmationPage(Context ctx) {
        // TODO: Fetch customer name or order ID from session if needed
        // ctx.attribute("customerName", ctx.sessionAttribute("customerName"));
        ctx.attribute("currentPage", "paymentConfirmation");
        ctx.render("payment_confirmation.html");
    }

    private static void renderAdminLoginPage(Context ctx) {
        String error = ctx.queryParam("error");
        ctx.attribute("error", error);
        ctx.render("admin_login.html");
    }

    private static void renderAdminDashboard(Context ctx, ConnectionPool connectionPool) {
        // TODO: Security check: if(ctx.sessionAttribute("adminUser") == null) { ctx.redirect(...); return; }
        // TODO: Fetch actual orders
        List<Object> orders = new ArrayList<>(); // Placeholder
        ctx.attribute("orders", orders);
        ctx.render("admin_dashboard.html");
    }

    private static void renderAdminOptionsPage(Context ctx, ConnectionPool connectionPool) {
        // TODO: Security check
        // TODO: Fetch actual options from DB
        List<Map<String, Object>> widths = new ArrayList<>(List.of(Map.of("value", 240, "price", 1000)));
        List<Map<String, Object>> lengths = new ArrayList<>(List.of(Map.of("value", 240, "price", 1500)));
        int trapezPrice = 500;
        ctx.attribute("widths", widths);
        ctx.attribute("lengths", lengths);
        ctx.attribute("trapezPrice", trapezPrice);
        ctx.render("admin_options.html");
    }

    private static void renderCreateAdminPage(Context ctx) {
        String message = ctx.queryParam("message");
        String email = ctx.queryParam("email");
        ctx.attribute("message", message);
        ctx.attribute("email", email);
        ctx.render("admin/create_admin.html");
    }


    // --- Form Handling Methods (All written out fully) ---

    private static void handleSpecificationsForm(Context ctx) {
        try {
            int width = ctx.formParamAsClass("carportWidth", Integer.class).get();
            int length = ctx.formParamAsClass("carportLength", Integer.class).get();
            boolean hasTrapezRoof = ctx.formParamAsClass("hasTrapezRoof", Boolean.class).getOrDefault(false);

            ctx.sessionAttribute("carportWidth", width);
            ctx.sessionAttribute("carportLength", length);
            ctx.sessionAttribute("hasTrapezRoof", hasTrapezRoof);
            System.out.println("Session Specs Saved: Width=" + width + ", Length=" + length + ", TrapezRoof=" + hasTrapezRoof);

            ctx.redirect(ROUTE_ADDITIONS);
        } catch (Exception e) {
            // Let general exception handler in startRouting handle this
            throw e;
        }
    }

    private static void handleAdditionsForm(Context ctx) {
        try {
            boolean includeShed = ctx.formParamAsClass("includeShed", Boolean.class).getOrDefault(false);
            String builder = ctx.formParamAsClass("builder", String.class).get(); // Assuming this field is always present
            boolean needsPaint = ctx.formParamAsClass("needsPaint", Boolean.class).getOrDefault(false);
            String specialRequests = ctx.formParam("specialRequests");

            ctx.sessionAttribute("includeShed", includeShed);
            ctx.sessionAttribute("builder", builder);
            ctx.sessionAttribute("needsPaint", needsPaint);
            ctx.sessionAttribute("specialRequests", specialRequests);
            System.out.println("Session Additions Saved: Shed=" + includeShed + ", Builder=" + builder + ", Paint=" + needsPaint + ", Requests=" + specialRequests);

            ctx.redirect(ROUTE_DETAILS);
        } catch (Exception e) {
            throw e; // Let general exception handler handle
        }
    }

    private static void handleDetailsForm(Context ctx, ConnectionPool connectionPool) throws Exception {
        try {
            String name = ctx.formParamAsClass("customerName", String.class).check(s -> s != null && !s.isBlank(), "Name is required").get();
            String address = ctx.formParamAsClass("customerAddress", String.class).check(s -> s != null && !s.isBlank(), "Address is required").get();
            String zip = ctx.formParamAsClass("customerZip", String.class).check(s -> s != null && s.matches("[0-9]{4}"), "Zip code must be 4 digits").get();
            String city = ctx.formParamAsClass("customerCity", String.class).check(s -> s != null && !s.isBlank(), "City is required").get();
            String phone = ctx.formParamAsClass("customerPhone", String.class).check(s -> s != null && s.matches("[0-9]{8}"), "Phone number must be 8 digits").get();
            String email = ctx.formParamAsClass("customerEmail", String.class).check(s -> s != null && s.contains("@"), "Valid email is required").get();
            boolean consent = ctx.formParamAsClass("consent", Boolean.class).getOrDefault(false);

            if (!consent) {
                System.err.println("Consent not given by user.");
                ctx.redirect(ROUTE_DETAILS + "?error=Du skal give samtykke for at fortsætte.");
                return;
            }

            Integer width = ctx.sessionAttribute("carportWidth");
            Integer length = ctx.sessionAttribute("carportLength");
            // ... retrieve all other session attributes ...

            if (width == null || length == null) {
                System.err.println("Error: Missing carport data in session for details submission.");
                throw new Exception("Incomplete carport data in session when submitting details.");
            }

            System.out.println("=== Order Details Received (Consent Given) ===");
            System.out.println("Customer: " + name); // Log other details...

            // TODO: Combine data, create Order object, save to DB, send email, clear session

            ctx.redirect(ROUTE_CONFIRMATION);

        } catch (ValidationException e) {
            System.err.println("Validation failed for details form: " + e.getErrors());
            ctx.redirect(ROUTE_DETAILS + "?error=Udfyld venligst alle felter korrekt.");
        } catch (Exception e) {
            // Let general exception handler handle other types of errors
            System.err.println("Exception in handleDetailsForm: " + e.getMessage());
            throw e;
        }
    }

    // handleAdminLoginAttempt and handleAdminCreateAttempt should call methods on AdminController instance
    // This is a simplified placeholder if AdminController is not passed to startRouting
    private static void handleAdminLoginAttempt(Context ctx) {
        String email = ctx.formParam("email");
        // This should actually call adminController.adminLogin(ctx);
        // Forcing redirect for now if AdminController is not setup in startRouting.
        System.out.println("Placeholder admin login attempt for: " + email + ". Redirecting to dashboard.");
        ctx.sessionAttribute("adminUser", email); // Simulate login
        ctx.redirect(ROUTE_ADMIN_DASHBOARD);
    }
}