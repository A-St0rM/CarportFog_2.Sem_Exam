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

import java.util.List;


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
            // 1. Hent data fra formular
            String name = ctx.formParam("customerName");
            String address = ctx.formParam("customerAddress");
            String phone = ctx.formParam("customerPhone");
            String email = ctx.formParam("customerEmail");
            int zip = Integer.parseInt(ctx.formParam("customerZip"));

            // 2. Slå by op i databasen
            String city = _postalCodeMapper.getCityByPostalCode(zip);

            // 3. Hvis by ikke findes, vis fejl og behold input
            if (city == null) {
                ctx.attribute("error", "Postnummeret findes ikke.");
                ctx.attribute("customerName", name);
                ctx.attribute("customerAddress", address);
                ctx.attribute("customerZip", zip);
                ctx.attribute("customerPhone", phone);
                ctx.attribute("customerEmail", email);
                ctx.render("details.html");
                return;
            }

            // 4. Opret Customer-objekt og gem i DB
            Customer customer = new Customer(email, address, phone, name, zip, city);
            Customer savedCustomer = _customerMapper.createCustomer(customer);

            // 5. Hent carportdata fra session
            int width = ctx.sessionAttribute("carportWidth");
            int length = ctx.sessionAttribute("carportLength");
            boolean trapezeRoof = ctx.sessionAttribute("hasTrapezRoof");

            // 6. Opret ordre uden totalpris
            Order order = new Order(0, width, length, "under_behandling", 0, savedCustomer, trapezeRoof);
            Order savedOrder = _orderMapper.insertOrder(order); // Får rigtigt orderId

            // 7. Beregn BOM med rigtig orderId
            _calculateBOM.calculateCarport(savedOrder);

            // 8. Beregn totalpris og opdater i DB
            int calculatedTotal = _calculateBOM.calculateTotalPriceFromBOM();
            savedOrder.setTotalPrice(calculatedTotal);
            _orderMapper.updateOrderTotalPrice(savedOrder.getOrderId(), calculatedTotal);

            // 9. Gem BOM-linjerne
            _orderMapper.insertBOMItems(_calculateBOM.getBom());

            // 10. Send tilbudsmail
            EmailService emailService = new EmailService();
            emailService.sendMailOffer(name, email, savedOrder.getTotalPrice());

            // 11. Ryd session og vis bekræftelse
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
            ctx.redirect("/admin_dashboard");
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

    public static void showSvg(Context ctx, ConnectionPool connectionPool) {
        CarportSvg svgDrawer = new CarportSvg(ctx);
        ctx.attribute("svg", svgDrawer.toString());
        ctx.render("details.html");
    }

    public void handleDeleteOrder(Context ctx, int orderId) {
        try {
            _orderMapper.deleteOrderById(orderId);
            ctx.redirect("/admin/dashboard"); // Tilbage til oversigt
        } catch (DatabaseException e) {
            ctx.status(500).result("Fejl ved sletning: " + e.getMessage());
        }
    }

    public void handlePaymentConfirmation(Context ctx) {
        Order order = ctx.sessionAttribute("order");

        if (order == null) {
            ctx.status(400).result("Ingen ordre fundet i session.");
            return;
        }

        try {
            _orderMapper.updateOrderStatus(order.getId(), "betalt");
            order.setStatus("betalt");
            ctx.sessionAttribute("order", order);
            ctx.attribute("customerName", order.getCustomer().getName());
            ctx.render("payment-confirmation.html");
        } catch (DatabaseException e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved betaling: " + e.getMessage());
        }
    }





}
