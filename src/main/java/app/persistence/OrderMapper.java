package app.persistence;

import app.entities.*;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper {

    private final ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public List<BOM> getBOMForOrder(int orderId) throws DatabaseException {

        List<BOM> bomList = new ArrayList<>();
        String query = "SELECT * FROM bill_of_products_view WHERE order_id = ?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setInt(1, orderId);
            var rs = preparedStatement.executeQuery();
            while (rs.next()) {

                // Order
                int carportWidth = rs.getInt("carport_width");
                int carportLength = rs.getInt("carport_length");
                int totalPrice = rs.getInt("total_price");
                boolean trapezRoof = rs.getBoolean("trapeze_roof");
                String status = rs.getString("status");

                Order order = new Order(carportWidth, carportLength, status, totalPrice, null, trapezRoof);

                // Product
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                int price = rs.getInt("price");

                Product product = new Product(productId, name, unit, price);

                // ProductVariant
                int productVariantId = rs.getInt("product_variant_id");
                int width = rs.getInt("width");
                String description = rs.getString("description");
                int length = rs.getInt("length");

                ProductVariant productVariant = new ProductVariant(productVariantId, length, width, product);

                // BOM
                int bomId = rs.getInt("order_item_id");
                int quantity = rs.getInt("quantity");

                BOM bom = new BOM(bomId, quantity, description, order, productVariant);
                bomList.add(bom);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente BOM fra databasen " + e.getMessage());
        }
        return bomList;
    }


    public Order insertOrder(Order order) throws DatabaseException {

        String query = "INSERT INTO orders (carport_width, carport_length, status, customer_id, total_price, trapeze_roof) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setInt(1, order.getCarportWidth());
            preparedStatement.setInt(2, order.getCarportLength());
            preparedStatement.setString(3, order.getStatus());
            preparedStatement.setInt(4, order.getCustomer().getCustomerId());
            preparedStatement.setInt(5, order.getTotalPrice());
            preparedStatement.setBoolean(6, order.getTrapezeRoof());

            preparedStatement.executeUpdate();

            ResultSet keySet = preparedStatement.getGeneratedKeys();
            if (keySet.next()) {
                int orderId = keySet.getInt(1);
                return new Order(orderId, order.getCarportWidth(), order.getCarportLength(),
                        order.getStatus(), order.getTotalPrice(), order.getCustomer(),
                        order.getTrapezeRoof());
            } else {
                throw new DatabaseException("Ingen genererede nøgler blev returneret efter indsættelsen af ordren.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke indsætte ordren ind i databasen: " + e.getMessage());
        }
    }

    public void insertBOMItems(List<BOM> bomlist) throws DatabaseException {

        String query = "INSERT INTO bom_Items (order_id, product_variant_id, quantity, description) VALUES (?,?,?,?)";

        try (Connection connection = connectionPool.getConnection()) {
            for (BOM bom : bomlist) {

                if (bom.getProductVariant() == null) {
                    continue; // Skips BOM if there's no variant
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, bom.getOrder().getId());
                    preparedStatement.setInt(2, bom.getProductVariant().getProductVariantId());
                    preparedStatement.setInt(3, bom.getQuantity());
                    preparedStatement.setString(4, bom.getDescription());
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke indsætte BOM Could not insert BOM items into the Database " + e.getMessage());
        }
    }


    public List<Order> getAllOrdersWithCustomerInfo() throws DatabaseException {
        List<Order> orders = new ArrayList<>();

        String sql = """
                    SELECT o.order_id, o.carport_width, o.carport_length, o.status, o.total_price, o.trapeze_roof,
                           c.customer_id, c.name, c.address, c.postal_code, c.phone, c.email,
                           pc.city
                    FROM orders o
                    JOIN customers c ON o.customer_id = c.customer_id
                    JOIN postal_codes pc ON c.postal_code = pc.postal_code
                """;


        try (Connection con = connectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("name"),
                        rs.getInt("postal_code"),
                        rs.getString("city")
                );

                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("carport_width"),
                        rs.getInt("carport_length"),
                        rs.getString("status"),
                        rs.getInt("total_price"),
                        customer,
                        rs.getBoolean("trapeze_roof")
                );

                orders.add(order);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af ordrer", e.getMessage());
        }

        return orders;
    }

    public Order getOrderById(int orderId) throws DatabaseException {
        String sql = """
                    SELECT o.*, c.customer_id, c.name AS customer_name, c.email, c.address, c.phone, c.postal_code, pc.city
                    FROM orders o
                    JOIN customers c ON o.customer_id = c.customer_id
                    JOIN postal_codes pc ON c.postal_code = pc.postal_code
                    WHERE o.order_id = ?
                """;


        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Customer
                    int customerId = rs.getInt("customer_id");
                    String customerName = rs.getString("customer_name");
                    String email = rs.getString("email");
                    String address = rs.getString("address");
                    String phone = rs.getString("phone");
                    int postalCode = rs.getInt("postal_code");
                    String city = rs.getString("city");

                    Customer customer = new Customer(customerId, email, address, phone, customerName, postalCode, city);

                    // Order
                    int width = rs.getInt("carport_width");
                    int length = rs.getInt("carport_length");
                    String status = rs.getString("status");
                    int totalPrice = rs.getInt("total_price");
                    boolean trapezRoof = rs.getBoolean("trapeze_roof");

                    return new Order(orderId, width, length, status, totalPrice, customer, trapezRoof);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente ordren: " + e.getMessage());
        }

        throw new DatabaseException("Ordre med ID " + orderId + " blev ikke fundet.");
    }

    public void updateOrderTotalPrice(int orderId, int newTotalPrice) throws DatabaseException {
        String sql = "UPDATE orders SET total_price = ? WHERE order_id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newTotalPrice);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke opdatere ordreprisen: " + e.getMessage());
        }
    }

    public void deleteOrderById(int orderId) throws DatabaseException {
        String deleteBOM = "DELETE FROM bom_items WHERE order_id = ?";
        String deleteOrder = "DELETE FROM orders WHERE order_id = ?";

        try (Connection conn = connectionPool.getConnection()) {

            // Delete BOM first
            try (PreparedStatement ps = conn.prepareStatement(deleteBOM)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

            // Delete the order itself
            try (PreparedStatement ps = conn.prepareStatement(deleteOrder)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke slette ordre: " + e.getMessage());
        }
    }

    public void updateOrderStatus(int orderId, String newStatus) throws DatabaseException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke opdatere ordre status: " + e.getMessage());
        }
    }


}
