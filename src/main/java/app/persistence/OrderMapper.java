package app.persistence;

import app.entities.BOMItem;
import app.entities.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

    public static List<Order> getAllOrders(ConnectionPool connectionPool) { //TODO:

        List<Order> orders = new ArrayList<Order>();
        String query = "SELECT * FROM orders";
        return orders;
    }

    public static List<BOMItem> getBOMItemByOrderId(ConnectionPool connectionPool, int orderId) {

        String query = "SELECT * FROM bill_of_materials_view WHERE order_id = ?";

    }


}
