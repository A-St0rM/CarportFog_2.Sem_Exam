package app.controllers;

import app.entities.Customer;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.OrderMapper;
import app.service.CalculateBOM;
import io.javalin.http.Context;


public class OrderController {

    private static void SendRequest(Context ctx, ConnectionPool connectionPool) {

        //Get order details from frontend
        int width = ctx.sessionAttribute("width");
        int length = ctx.sessionAttribute("length");
        int totalPrice = 1999; //TODO: hardcoded for now

        Customer customer = new Customer(1, "Alissa Andrea Storm", "Alissa@hotmail.com", "Voltvej 5", "2134343", 2980); //TODO: hardcoded for now

        Order order = new Order(0, width, length, "not paid", totalPrice, customer);
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
}
