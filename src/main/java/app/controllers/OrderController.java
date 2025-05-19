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
                ctx.attribute("svg", ctx.sessionAttribute("svg"));

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

            ctx.attribute("svg", ctx.sessionAttribute("svg"));
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

    public static void showSvg(Context ctx) {
        CarportSvg svgDrawer = new CarportSvg(ctx);
        String svg = svgDrawer.toString(); // gem som variabel

        ctx.attribute("svg", svg);             // til første visning
        ctx.sessionAttribute("svg", svg);      // til brug ved fejl (fx forkert postnummer)
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

        try {
            _orderMapper.updateOrderStatus(order.getId(), "betalt");
            order.setStatus("betalt");

            List<BOM> bomListFromDB = _orderMapper.getBOMForOrder(order.getId());
            List<Map<String, Object>> bomItemsForEmail = new ArrayList<>();

            for (BOM bomItem : bomListFromDB) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("description", bomItem.getDescription());
                itemMap.put("quantity", bomItem.getQuantity());
                // itemMap.put("unit", bomItem.getUnit()); // Tilføj hvis din BOM-klasse har getUnit()
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


            // Gem ordren i sessionen, så den er nem at tilgå efter "betaling"
            // Dette er vigtigt for din handlePaymentConfirmation metode
            ctx.sessionAttribute("order", order);

            // Send ordreoplysninger til HTML-skabelonen
            ctx.attribute("order", order);
            ctx.attribute("customer", order.getCustomer());
            ctx.attribute("totalPrice", order.getTotalPrice());
            ctx.attribute("orderId", order.getId()); // Send orderId med for formular action

            ctx.render("payment.html"); // Din nye HTML-side for betaling

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
