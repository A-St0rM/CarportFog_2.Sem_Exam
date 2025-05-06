package app.controllers;

import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
// import app.entities.Order;

public class RoutingController {

    // Define route paths as constants
    private static final String ROUTE_START = "/";
    private static final String ROUTE_SPECIFICATIONS = "/specifications";
    private static final String ROUTE_ADDITIONS = "/additions";
    private static final String ROUTE_DETAILS = "/details";
    private static final String ROUTE_CONFIRMATION = "/confirmation";
    private static final String ROUTE_ADMIN_LOGIN = "/admin/login";
    private static final String ROUTE_ADMIN_DASHBOARD = "/admin/dashboard";
    private static final String ROUTE_ADMIN_OPTIONS = "/admin/options";

    public static void startRouting(Javalin app, ConnectionPool connectionPool) {

        // --- GET Routes ---
        app.get(ROUTE_START, RoutingController::renderStartPage);
        app.get(ROUTE_SPECIFICATIONS, ctx -> renderSpecificationsPage(ctx, connectionPool));
        app.get(ROUTE_ADDITIONS, ctx -> renderAdditionsPage(ctx, connectionPool));
        app.get(ROUTE_DETAILS, ctx -> renderDetailsPage(ctx, connectionPool)); // Will now check for query param 'error'
        app.get(ROUTE_CONFIRMATION, RoutingController::renderConfirmationPage);
        app.get(ROUTE_ADMIN_LOGIN, RoutingController::renderAdminLoginPage);
        app.get(ROUTE_ADMIN_DASHBOARD, ctx -> renderAdminDashboard(ctx, connectionPool));
        app.get(ROUTE_ADMIN_OPTIONS, ctx -> renderAdminOptionsPage(ctx, connectionPool));

        // --- POST Routes ---
        app.post(ROUTE_SPECIFICATIONS, ctx -> handleSpecificationsForm(ctx));
        app.post(ROUTE_ADDITIONS, ctx -> handleAdditionsForm(ctx));
        app.post(ROUTE_DETAILS, ctx -> handleDetailsForm(ctx, connectionPool)); // Handler updated below
        app.post(ROUTE_ADMIN_LOGIN, ctx -> handleAdminLoginAttempt(ctx));
        // TODO: app.post(ROUTE_ADMIN_OPTIONS, ...)

        // --- Exception Handling ---
        app.exception(ValidationException.class, (e, ctx) -> {
            // TODO: Improve this - re-render form with specific errors
            System.err.println("Validation Error occurred: " + e.getErrors());
            ctx.attribute("error", "Udfyld venligst alle påkrævede felter korrekt."); // Generic message
            // Need context to know which page to re-render, complicates central handling
            // Example: if (ctx.path().equals(ROUTE_DETAILS)) { renderDetailsPage(ctx, connectionPool); } // Needs pool instance
            // Fallback for now:
            ctx.status(400).result("Validation Error: Please check your input. " + e.getErrors());
        });
        app.exception(Exception.class, (e, ctx) -> {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).result("Internal server error.");
        });
    }

    // --- Page Rendering Methods ---

    private static void renderStartPage(Context ctx) { /* as before */ ctx.attribute("currentPage", "start"); ctx.render("index.html"); }
    private static void renderSpecificationsPage(Context ctx, ConnectionPool cp) { /* as before */ ctx.attribute("currentPage", "specifications"); ctx.render("specifications.html"); }
    private static void renderAdditionsPage(Context ctx, ConnectionPool cp) { /* as before */ ctx.attribute("currentPage", "additions"); ctx.render("additions.html"); }

    // *** UPDATED renderDetailsPage ***
    private static void renderDetailsPage(Context ctx, ConnectionPool connectionPool) {
        // TODO: Fetch saved values from session
        // *** NEW: Check for error message in query parameter ***
        String error = ctx.queryParam("error");
        ctx.attribute("error", error); // Pass error (or null) to the template

        ctx.attribute("currentPage", "details");
        ctx.render("details.html");
    }

    private static void renderConfirmationPage(Context ctx) { /* as before */ ctx.attribute("currentPage", "confirmation"); ctx.render("confirmation.html"); }
    private static void renderAdminLoginPage(Context ctx) { /* as before */ ctx.attribute("error", ctx.queryParam("error")); ctx.render("admin_login.html"); }
    private static void renderAdminDashboard(Context ctx, ConnectionPool cp) { /* as before */ List<Object> o=new ArrayList<>(); ctx.attribute("orders",o); ctx.render("admin_dashboard.html"); }
    private static void renderAdminOptionsPage(Context ctx, ConnectionPool cp) { /* as before */ List<Map<String,Object>> w=new ArrayList<>(List.of(Map.of("value",240,"price",1000))); List<Map<String,Object>> l=new ArrayList<>(List.of(Map.of("value",240,"price",1500))); int tp=500; ctx.attribute("widths",w); ctx.attribute("lengths",l); ctx.attribute("trapezPrice",tp); ctx.render("admin_options.html"); }


    // --- Form Handling Methods ---

    private static void handleSpecificationsForm(Context ctx) { /* as before */ try { int w=ctx.formParamAsClass("carportWidth",Integer.class).get(); int l=ctx.formParamAsClass("carportLength",Integer.class).get(); boolean h=ctx.formParamAsClass("hasTrapezRoof",Boolean.class).getOrDefault(false); ctx.sessionAttribute("carportWidth",w); ctx.sessionAttribute("carportLength",l); ctx.sessionAttribute("hasTrapezRoof",h); ctx.redirect(ROUTE_ADDITIONS); } catch(Exception e){throw e;} }
    private static void handleAdditionsForm(Context ctx) { /* as before */ try { boolean i=ctx.formParamAsClass("includeShed",Boolean.class).getOrDefault(false); String b=ctx.formParamAsClass("builder",String.class).get(); boolean n=ctx.formParamAsClass("needsPaint",Boolean.class).getOrDefault(false); String s=ctx.formParam("specialRequests"); ctx.sessionAttribute("includeShed",i); ctx.sessionAttribute("builder",b); ctx.sessionAttribute("needsPaint",n); ctx.sessionAttribute("specialRequests",s); ctx.redirect(ROUTE_DETAILS); } catch(Exception e){throw e;} }

    // *** UPDATED handleDetailsForm ***
    private static void handleDetailsForm(Context ctx, ConnectionPool connectionPool) throws Exception {
        try {
            // 1. Read customer details
            String name = ctx.formParamAsClass("customerName", String.class).check(s -> s != null && !s.isBlank(), "Name is required").get();
            String address = ctx.formParamAsClass("customerAddress", String.class).check(s -> s != null && !s.isBlank(), "Address is required").get();
            String zip = ctx.formParamAsClass("customerZip", String.class).check(s -> s != null && s.matches("[0-9]{4}"), "Zip code must be 4 digits").get();
            String city = ctx.formParamAsClass("customerCity", String.class).check(s -> s != null && !s.isBlank(), "City is required").get();
            String phone = ctx.formParamAsClass("customerPhone", String.class).check(s -> s != null && s.matches("[0-9]{8}"), "Phone number must be 8 digits").get();
            String email = ctx.formParamAsClass("customerEmail", String.class).check(s -> s != null && s.contains("@"), "Valid email is required").get();
            boolean consent = ctx.formParamAsClass("consent", Boolean.class).getOrDefault(false); // Read consent value

            // **** REPLACED 'throw' WITH REDIRECT ****
            if (!consent) {
                System.err.println("Consent not given by user.");
                // Redirect back to details page with an error message in URL
                ctx.redirect(ROUTE_DETAILS + "?error=Du skal give samtykke for at fortsætte.");
                return; // Stop processing this request further
            }
            // ****************************************

            // 2. Retrieve carport details from session (if consent was given)
            Integer width = ctx.sessionAttribute("carportWidth");
            Integer length = ctx.sessionAttribute("carportLength");
            Boolean hasTrapezRoof = ctx.sessionAttribute("hasTrapezRoof");
            Boolean includeShed = ctx.sessionAttribute("includeShed");
            String builder = ctx.sessionAttribute("builder");
            Boolean needsPaint = ctx.sessionAttribute("needsPaint");
            String specialRequests = ctx.sessionAttribute("specialRequests");

            // 3. TODO: Validate that session data exists
            if (width == null || length == null) {
                System.err.println("Error: Missing carport data in session.");
                throw new Exception("Incomplete carport data in session.");
            }

            // 4. TODO: Combine all data & Create Order object
            System.out.println("=== Order Details Received (Consent Given) ===");
            System.out.println("Customer: " + name);

            // 5. *** TODO: SAVE THE COMBINED ORDER DATA TO THE DATABASE ***

            // 6. TODO: Send confirmation email

            // 7. TODO: Clear session attributes?

            // 8. Redirect to the confirmation page
            ctx.redirect(ROUTE_CONFIRMATION);

        } catch (ValidationException e) {
            // TODO: Re-render details form with *specific* error messages and *all* submitted data
            System.err.println("Validation failed for details: " + e.getErrors());
            // Example redirect back with generic error for now
            ctx.redirect(ROUTE_DETAILS + "?error=Udfyld venligst alle felter korrekt.");
            // throw e; // Avoid re-throwing if redirecting
        } catch (Exception e) {
            System.err.println("Error in handleDetailsForm: " + e.getMessage());
            throw e; // Let general handler catch other errors
        }
    }

    private static void handleAdminLoginAttempt(Context ctx) { /* as before */ try{String e=ctx.formParam("email");boolean i=true;if(i){ctx.sessionAttribute("adminUser",e);ctx.redirect(ROUTE_ADMIN_DASHBOARD);}else{ctx.redirect(ROUTE_ADMIN_LOGIN+"?error=Invalid");}}catch(Exception ex){throw ex;} }

    // TODO: Implement handleAdminOptionsUpdate(...)
}