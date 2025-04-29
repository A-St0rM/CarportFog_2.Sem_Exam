package app.controllers;

import app.injectorHandler.DependencyInjector; // Antager denne findes og virker som ønsket
import app.persistence.ConnectionPool;
import io.javalin.Javalin;
import io.javalin.http.Context; // Importer Context

import java.util.Map; // Importer Map

public class RoutingController {

    // Vi definerer rute-stier som konstanter for nem reference
    private static final String ROUTE_START = "/";
    private static final String ROUTE_SPECIFIKATIONER = "/specifikationer";
    private static final String ROUTE_TILFOEJELSER = "/tilfoejelser";
    private static final String ROUTE_OPLYSNINGER = "/oplysninger";
    // Tilføj flere ruter efter behov (f.eks. til admin, ordre-bekræftelse etc.)

    public static void startRouting(Javalin app, ConnectionPool connectionPool) {


        DependencyInjector di = new DependencyInjector(connectionPool);

        // Definer routes for hver side
        app.get(ROUTE_START, ctx -> renderStartPage(ctx));
        app.get(ROUTE_SPECIFIKATIONER, ctx -> renderSpecifikationerPage(ctx));
        app.get(ROUTE_TILFOEJELSER, ctx -> renderTilfoejelserPage(ctx));
        app.get(ROUTE_OPLYSNINGER, ctx -> renderOplysningerPage(ctx));

    }


    private static void renderStartPage(Context ctx) {
        // Send model med info om nuværende side til Thymeleaf
        ctx.attribute("currentPage", "start"); // Bruges til at sætte 'active' klasse i HTML
        ctx.render("index.html"); // Antager filen findes i resources/templates
    }


    private static void renderSpecifikationerPage(Context ctx) {
        // TODO: Hent evt. nødvendige data fra database/session her
        ctx.attribute("currentPage", "specifikationer");
        ctx.render("specifikationer.html");
    }

    // Metode til at rendere Tilføjelser-siden
    private static void renderTilfoejelserPage(Context ctx) {
        // TODO: Hent evt. nødvendige data
        ctx.attribute("currentPage", "tilfoejelser");
        ctx.render("tilfoejelser.html");
    }

    // Metode til at rendere Oplysninger-siden
    private static void renderOplysningerPage(Context ctx) {
        // TODO: Hent evt. nødvendige data
        ctx.attribute("currentPage", "oplysninger");
        ctx.render("oplysninger.html");
    }

    /*
    private static void handleSpecifikationerForm(Context ctx) {
        // Hent data fra ctx.formParam("...")
        // Valider data
        // Gem data (i session? database?)
        // Redirect til næste side (f.eks. Tilføjelser)
        ctx.redirect(ROUTE_TILFOEJELSER);
    }
    */
}