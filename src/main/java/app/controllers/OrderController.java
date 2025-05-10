package app.controllers;

import app.entities.Customer;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.CustomerMapper;
import app.persistence.OrderMapper;
import app.service.CalculateBOM;
import io.javalin.http.Context;

import java.sql.SQLException;


public class OrderController {

    private static void SendRequest(Context ctx, ConnectionPool connectionPool) {

        //Get order details from frontend
        int width = ctx.sessionAttribute("width");
        int length = ctx.sessionAttribute("length");
        String status = "Not paid";
        boolean trapezeRoof = false; //TODO: hardcoded for now
        int totalPrice = 1999; //TODO: hardcoded for now

        Customer customer = new Customer(1,"Voltvej 5", "21343432","999999999", "Lars",2100); //TODO: hardcoded for now

        Order order = new Order(0, width, length, status, totalPrice, customer, trapezeRoof);
        // insert order in database

        try{
            order = OrderMapper.insertOrder(order, connectionPool);

            //calculate bom items
            CalculateBOM calculateBOM = new CalculateBOM(width, length, connectionPool);
            calculateBOM.calculateCarport(order);

            //save bom items in database
            OrderMapper.insertBOMItems(calculateBOM.getBom(), connectionPool);

            //create message to customer and render order /request confirmation
            ctx.render("orderflow/requestconfirmation.html");

        } catch (DatabaseException e) { //TODO: handle exception later
            throw new RuntimeException(e);
        }

    }

    public static void handleDetailsPost(Context ctx, ConnectionPool connectionPool) throws SQLException {
        // Hent data fra formularen, ikke sessionen
        String name = ctx.formParam("customerName");
        String address = ctx.formParam("customerAddress");
        int postalCode = Integer.parseInt(ctx.formParam("customerZip"));
        String city = ctx.formParam("customerCity");
        String phone = ctx.formParam("customerPhone");
        String email = ctx.formParam("customerEmail");

        // Opret kunde og gem i databasen
        Customer customer = new Customer(email, address, phone, name, postalCode);
        Customer savedCustomer = CustomerMapper.createCustomer(customer, connectionPool);

    }
}
