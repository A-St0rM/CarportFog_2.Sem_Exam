package app.controllers;

import app.entities.BOM;
import app.entities.Customer;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.CustomerMapper;
import app.persistence.OrderMapper;
import app.service.CalculateBOM;
import app.service.EmailService;
import io.javalin.http.Context;

import java.util.List;


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

    public void handleSpecificationsPost(Context ctx) {
        int width = Integer.parseInt(ctx.formParam("carportWidth"));
        int length = Integer.parseInt(ctx.formParam("carportLength"));
        boolean trapezeRoof = Boolean.parseBoolean(ctx.formParam("hasTrapezRoof"));
        ctx.sessionAttribute("carportWidth", width);
        ctx.sessionAttribute("carportLength", length);
        ctx.sessionAttribute("hasTrapezRoof", trapezeRoof);

        ctx.redirect("/additions");
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
            EmailService emailService = new EmailService();

            int width = ctx.sessionAttribute("carportWidth");
            int length = ctx.sessionAttribute("carportLength");
            boolean trapezeRoof = ctx.sessionAttribute("hasTrapezRoof");

            // 4. Create order WITHOUT totalPrice yet
            Order order = new Order(0, width, length, "Not paid", 0, savedCustomer, trapezeRoof);
            Order savedOrder = _orderMapper.insertOrder(order);

            // 5. Calculate BOM
            _calculateBOM.calculateCarport(savedOrder);
            int calculatedTotal = _calculateBOM.calculateTotalPriceFromBOM();

            // 6. Update order with totalPrice
            _orderMapper.updateOrderTotalPrice(savedOrder.getOrderId(), calculatedTotal);

            // 7. Save BOM items
            _orderMapper.insertBOMItems(_calculateBOM.getBom());

            //8
            emailService.sendMailOffer(name, email, zip, order.getTotalPrice());

            // 9. Clean up session and redirect
            ctx.req().getSession().invalidate();
            ctx.render("confirmation.html");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error saving order.");
        }
    }


    public void showAllOrders(Context ctx) {
        try {
            List<Order> orders = _orderMapper.getAllOrdersWithCustomerInfo();
            ctx.attribute("orders", orders);
            ctx.render("admin_dashboard.html");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved hentning af ordrer");
        }
    }

    public void showBOMPage(Context ctx) {
        int orderId = Integer.parseInt(ctx.pathParam("orderId"));

        try {
            Order order = _orderMapper.getOrderById(orderId);
            List<BOM> bomList = _orderMapper.getBOMForOrder(orderId);

            ctx.attribute("order", order);
            ctx.attribute("bomList", bomList);
            ctx.render("bom_view.html");
        } catch (Exception e) {
            ctx.status(500).result("Kunne ikke hente stykliste.");
        }
    }

    public void handleUpdateTotalPrice(Context ctx) {
        int orderId = Integer.parseInt(ctx.pathParam("orderId"));
        int newTotalPrice = Integer.parseInt(ctx.formParam("newTotalPrice"));

        try {
            _orderMapper.updateOrderTotalPrice(orderId, newTotalPrice);
            ctx.redirect("/admin_dashboard"); // eller redirect til styklisten igen hvis ønsket
        } catch (DatabaseException e) {
            e.printStackTrace();
            ctx.status(500).result("Kunne ikke opdatere prisen: " + e.getMessage());
        }
    }








}
