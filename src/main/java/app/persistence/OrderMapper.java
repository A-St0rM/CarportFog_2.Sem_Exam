package app.persistence;

import app.entities.BOM;
import app.entities.Product;
import app.entities.ProductVariant;
import app.entities.Order;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

//    public static List<Order> getAllOrders(ConnectionPool connectionPool) { //TODO:
//
//        List<Order> orders = new ArrayList<Order>();
//        String query = "SELECT * FROM orders";
//        return orders;
//    }

    public static List<BOM> getBOMItemsByOrderId(ConnectionPool connectionPool, int orderId) throws DatabaseException {

        List<BOM> bomList = new ArrayList<>();
        String query = "SELECT * FROM bill_of_products_view WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setInt(1, orderId);
            var rs = preparedStatement.executeQuery();
            while (rs.next()) {

                //Order
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                boolean isPaid = rs.getBoolean("is_paid");
                int totalPrice = rs.getInt("total_price");
                boolean trapezRoof = rs.getBoolean("trapez_roof");
                String status = rs.getString("status");

                Order order = new Order(carportWidth, carportLength, status, totalPrice, null, trapezRoof);

                //product
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                int price = rs.getInt("price");

                Product product = new Product(productId, name, unit, price);

                //productVariant
                int productVariantId = rs.getInt("product_variant_id");
                String description = rs.getString("description");
                int length = rs.getInt("length");

                ProductVariant productVariant = new ProductVariant(productVariantId, length, product);

                //BOM
                int bomId = rs.getInt("bom_id");
                int quantity = rs.getInt("quantity");

                BOM bom = new BOM(bomId, quantity, description, order, productVariant);
                bomList.add(bom);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not get BOM from the Database " + e.getMessage());
        }
        return bomList;
    }


    public static Order insertOrder(Order order, ConnectionPool connectionPool) throws DatabaseException {

        String query = "INSERT INTO orders (carport_width, carport_length, is_paid, customer_id, total_price)" +
                "VALUES(?,?,?,?,?)";
        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        ) {
            preparedStatement.setInt(1, order.getCarportWidth());
            preparedStatement.setInt(2, order.getCarportLength());
            preparedStatement.setBoolean(3, false);
            preparedStatement.setInt(4, order.getCustomer().getCustomerId());
            preparedStatement.setInt(5, order.getTotalPrice());
            preparedStatement.setBoolean(6, order.getTrapezeRoof());
            preparedStatement.executeUpdate();

            ResultSet keySet = preparedStatement.getGeneratedKeys();

            if (keySet.next()) {
                Order newOrder = new Order(keySet.getInt(1), order.getCarportWidth(), order.getCarportLength(), order.getStatus(), order.getTotalPrice(), order.getCustomer(), order.getTrapezeRoof());
                return newOrder;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not insert order into the Database " + e.getMessage());
        }
    }

    public static void insertBOMItems(List<BOM> bomlist, ConnectionPool connectionPool) throws DatabaseException {

        String query = "INSERT INTO bom_Items (order_id, product_variant_id, quantity, description)" + "VALUES(?,?,?,?)";

        try (Connection connection = connectionPool.getConnection()) {
            for (BOM bom : bomlist) {

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, bom.getOrder().getOrderId());
                    preparedStatement.setInt(2, bom.getProductVariant().getProductVariantId());
                    preparedStatement.setInt(3, bom.getQuantity());
                    preparedStatement.setString(4, bom.getDescription());
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not insert BOM items into the Database " + e.getMessage());
        }

    }

}
