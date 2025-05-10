package app.controllers;

import app.entities.Customer;
import app.entities.Order;
import app.persistence.ConnectionPool;
import app.persistence.CustomerMapper;
import app.persistence.OrderMapper;
import app.service.CalculateBOM;
import io.javalin.http.Context;



public class OrderController {

    private final OrderMapper _orderMapper;
    private final ConnectionPool _connectionPool;
    private final CustomerMapper _customerMapper;
    private final CalculateBOM _calculateBOM;

    public OrderController(OrderMapper orderMapper, ConnectionPool connectionPool, CustomerMapper customerMapper, CalculateBOM calculateBOM)
    {
        this._orderMapper = orderMapper;
        this._connectionPool = connectionPool;
        this._customerMapper = customerMapper;
        this._calculateBOM = calculateBOM;
    }

//    private void sendRequest(Context ctx) {
//
//        //Get order details from frontend
//        int width = ctx.sessionAttribute("width");
//        int length = ctx.sessionAttribute("length");
//        String status = "Not paid";
//        boolean trapezeRoof = false; //TODO: hardcoded for now
//        int totalPrice = 1999; //TODO: hardcoded for now
//
//        Customer customer = new Customer(1,"Voltvej 5", "21343432","999999999", "Lars",2100); //TODO: hardcoded for now
//
//        Order order = new Order(0, width, length, status, totalPrice, customer, trapezeRoof);
//        // insert order in database
//
//        try{
//            order = _orderMapper.insertOrder(order);
//
//            //calculate bom items
//            _calculateBOM.calculateCarport(order);
//
//            //save bom items in database
//            _orderMapper.insertBOMItems(_calculateBOM.getBom());
//
//            //create message to customer and render order /request confirmation
//            ctx.render("orderflow/requestconfirmation.html");
//
//        } catch (DatabaseException e) { //TODO: handle exception later
//            throw new RuntimeException(e);
//        }
//    }

    public void handleSpecificationsPost(Context ctx) {
        int width = Integer.parseInt(ctx.formParam("width"));
        int length = Integer.parseInt(ctx.formParam("length"));
        ctx.sessionAttribute("width", width);
        ctx.sessionAttribute("length", length);
        boolean trapezeRoof = Boolean.parseBoolean(ctx.formParam("trapezeRoof"));
        ctx.sessionAttribute("trapezeRoof", trapezeRoof);

        ctx.redirect("/specifications");
    }

    public void handleAdditionsPost(Context ctx) {
        boolean trapezeRoof = Boolean.parseBoolean(ctx.formParam("trapezeRoof"));
        ctx.sessionAttribute("trapezeRoof", trapezeRoof);

        ctx.redirect("/details"); // Go to contact info
    }

    public void handleDetailsPost(Context ctx) {
        String name = ctx.formParam("customerName");
        String address = ctx.formParam("customerAddress");
        int zip = Integer.parseInt(ctx.formParam("customerZip"));
        String phone = ctx.formParam("customerPhone");
        String email = ctx.formParam("customerEmail");

        Customer customer = new Customer(email, address, phone, name, zip);

        try {
            Customer savedCustomer = _customerMapper.createCustomer(customer);

            // 3. Get earlier step data from session
            int width = ctx.sessionAttribute("width");
            int length = ctx.sessionAttribute("length");
            boolean trapezeRoof = ctx.sessionAttribute("trapezeRoof");
            int totalPrice = 1999; // Placeholder
            String status = "Not paid";

            // 4. Create + save order
            Order order = new Order(0, width, length, status, totalPrice, savedCustomer, trapezeRoof);
            Order savedOrder = _orderMapper.insertOrder(order);

            // 5. Calculate BOM + save
            _calculateBOM.calculateCarport(savedOrder);
            _orderMapper.insertBOMItems(_calculateBOM.getBom());

            // 6. Clear session and confirm
            ctx.req().getSession().invalidate();
            ctx.render("confirmation.html");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error saving order.");
        }
    }
}
