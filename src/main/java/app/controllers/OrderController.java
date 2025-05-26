package app.controllers;

import app.entities.BOM;
import app.entities.Customer;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.CustomerMapper;
import app.persistence.OrderMapper;
import app.persistence.PostalCodeMapper;
import app.service.CalculateBOM;
import app.service.EmailService;
import app.service.CarportSvg;
import app.service.Svg;
import io.javalin.http.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;


public class OrderController {

    private final OrderMapper _orderMapper;
    private final ConnectionPool _connectionPool;
    private final CustomerMapper _customerMapper;
    private final CalculateBOM _calculateBOM;
    private final PostalCodeMapper _postalCodeMapper;


    public OrderController(OrderMapper orderMapper, ConnectionPool connectionPool, CustomerMapper customerMapper, CalculateBOM calculateBOM, PostalCodeMapper postalCodeMapper)
    {
        this._orderMapper = orderMapper;
        this._connectionPool = connectionPool;
        this._customerMapper = customerMapper;
        this._calculateBOM = calculateBOM;
        this._postalCodeMapper = postalCodeMapper;
    }

    public void handleSpecificationsPost(Context ctx) {
        int width = Integer.parseInt(ctx.formParam("carportWidth"));
        int length = Integer.parseInt(ctx.formParam("carportLength"));
        boolean trapezeRoof = Boolean.parseBoolean(ctx.formParam("hasTrapezRoof"));
        ctx.sessionAttribute("carportWidth", width);
        ctx.sessionAttribute("carportLength", length);
        ctx.sessionAttribute("hasTrapezRoof", trapezeRoof);

        ctx.redirect("/details");
    }

    public void handleDetailsPost(Context ctx) {
        try {
            // Retrieves data from form
            String name = ctx.formParam("customerName");
            String address = ctx.formParam("customerAddress");
            String phone = ctx.formParam("customerPhone");
            String email = ctx.formParam("customerEmail");
            int zip = Integer.parseInt(ctx.formParam("customerZip"));

            // Finds the targeted city in the database
            String city = _postalCodeMapper.getCityByPostalCode(zip);

            // If city doesn't exist, show error and keep input
            if (city == null) {
                ctx.attribute("error", "Postnummeret findes ikke.");
                ctx.attribute("customerName", name);
                ctx.attribute("customerAddress", address);
                ctx.attribute("customerZip", zip);
                ctx.attribute("customerPhone", phone);
                ctx.attribute("customerEmail", email);
                ctx.attribute("svg", ctx.sessionAttribute("svg"));

                ctx.render("details.html");
                return;
            }

            // Create customer-object and save in database
            Customer customer = new Customer(email, address, phone, name, zip, city);
            Customer savedCustomer = _customerMapper.createCustomer(customer);

            // Get carport data from session
            int width = ctx.sessionAttribute("carportWidth");
            int length = ctx.sessionAttribute("carportLength");
            boolean trapezeRoof = ctx.sessionAttribute("hasTrapezRoof");

            // Create order without total price
            Order order = new Order(0, width, length, "under_behandling", 0, savedCustomer, trapezeRoof);
            Order savedOrder = _orderMapper.insertOrder(order); // Retrieves correct orderId

            // Calculate BOM with correct orderId
            _calculateBOM.calculateCarport(savedOrder);

            // Calculate total price and updates in database
            int calculatedTotal = _calculateBOM.calculateTotalPriceFromBOM();
            savedOrder.setTotalPrice(calculatedTotal);
            _orderMapper.updateOrderTotalPrice(savedOrder.getOrderId(), calculatedTotal);

            // Saves BOM-lines
            _orderMapper.insertBOMItems(_calculateBOM.getBom());

            // Sends e-mail with offer
            EmailService emailService = new EmailService();
            emailService.sendMailOffer(name, email, savedOrder.getTotalPrice());

            ctx.attribute("svg", ctx.sessionAttribute("svg"));

            // Invalidates (cleans) session and shows confirmation
            ctx.req().getSession().invalidate();
            ctx.render("confirmation.html");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved oprettelse af ordre.");
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
            ctx.redirect("/admin/dashboard");
        } catch (DatabaseException e) {
            e.printStackTrace();
            ctx.status(500).result("Kunne ikke opdatere prisen: " + e.getMessage());
        }
    }

    public void handleSendPaymentEmail(Context ctx) {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try {
            Order order = _orderMapper.getOrderById(orderId);
            Customer customer = order.getCustomer();

            _orderMapper.updateOrderStatus(orderId, "afventende");

            EmailService emailService = new EmailService();
            boolean result = emailService.sendMailPayment(
                    customer.getName(),
                    customer.getEmail(),
                    orderId
            );
            if (result) {
                ctx.redirect("/admin/order/" + orderId + "/bom");
            } else {
                ctx.status(500).result("Fejl: Mailen kunne ikke sendes");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved afsendelse af e-mail");
        }
    }

    public static void showSvg(Context ctx) {
        CarportSvg svgDrawer = new CarportSvg(ctx);
        String svg = svgDrawer.toString(); // saves as a variable

        ctx.attribute("svg", svg);             // for the first viewing of the svg
        ctx.sessionAttribute("svg", svg);      // for  when errors happen (for example wrong postal code)
        ctx.render("details.html");
    }


    public void handleDeleteOrder(Context ctx, int orderId) {
        try {
            _orderMapper.deleteOrderById(orderId);
            ctx.redirect("/admin/dashboard");
        } catch (DatabaseException e) {
            ctx.status(500).result("Fejl ved sletning: " + e.getMessage());
        }
    }

    public void handlePaymentConfirmation(Context ctx) {
        Order order = ctx.sessionAttribute("order");

        try {
            _orderMapper.updateOrderStatus(order.getId(), "betalt");
            order.setStatus("betalt");

            List<BOM> bomListFromDB = _orderMapper.getBOMForOrder(order.getId());
            List<Map<String, Object>> bomItemsForEmail = new ArrayList<>();

            for (BOM bomItem : bomListFromDB) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("description", bomItem.getDescription());
                itemMap.put("quantity", bomItem.getQuantity());
                bomItemsForEmail.add(itemMap);
            }

            EmailService emailService = new EmailService();
            emailService.sendMailConfirmation(
                    order.getCustomer().getName(),
                    order.getCustomer().getEmail(),
                    order.getTotalPrice(),
                    bomItemsForEmail
            );

            ctx.sessionAttribute("order", order);
            ctx.attribute("customerName", order.getCustomer().getName());
            ctx.render("payment_confirmation.html");

        } catch (DatabaseException e) {
            e.printStackTrace();
            ctx.status(500).result("Databasefejl ved betalingsbekræftelse: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved afsendelse af bekræftelsesmail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Uventet fejl ved betalingsbekræftelse: " + e.getMessage());
        }
    }


    public void showPaymentPage(Context ctx) {
        try {
            int orderId = Integer.parseInt(ctx.queryParam("orderId"));
            Order order = _orderMapper.getOrderById(orderId);

            if (order == null) {
                ctx.status(404).result("Fejl: Ordre ikke fundet.");
                return;
            }


            // Saves order in session, so it's easier to access after payment
            ctx.sessionAttribute("order", order);

            // Sends order information to the HTML-template
            ctx.attribute("order", order);
            ctx.attribute("customer", order.getCustomer());
            ctx.attribute("totalPrice", order.getTotalPrice());
            ctx.attribute("orderId", order.getId());

            ctx.render("payment.html");

        } catch (NumberFormatException e) {
            ctx.status(400).result("Fejl: Ugyldigt orderId format.");
        } catch (DatabaseException e) {
            e.printStackTrace();
            ctx.status(500).result("Databasefejl ved hentning af ordre til betaling.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Uventet fejl ved visning af betalingsside.");
        }
    }

}
