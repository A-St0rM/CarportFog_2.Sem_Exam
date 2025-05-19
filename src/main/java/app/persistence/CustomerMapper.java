package app.persistence;

import app.entities.Customer;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CustomerMapper {

    private final ConnectionPool connectionPool;


    public CustomerMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public List<Customer> getCustomersByCustomerId(ConnectionPool connectionPool, int customerId) throws DatabaseException {

        List<Customer> customerList = new ArrayList<>();
        String sql = """
                    SELECT c.customer_id, c.name, c.address, c.phone, c.email, c.postal_code,
                           p.city
                    FROM customers c
                    JOIN postal_codes p ON c.postal_code = p.postal_code
                    WHERE c.customer_id = ?
                """;
        try (
                PreparedStatement preparedStatement = connectionPool.getConnection().prepareStatement(sql)
        ) {
            preparedStatement.setInt(1, customerId);
            var rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String address = rs.getString("address");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                int postalCode = rs.getInt("postalCode");
                String city = rs.getString("city");

                Customer customer;
                customer = new Customer(email, address, phone, name, postalCode, city);
                customerList.add(customer);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return customerList;
    }

    public Customer createCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (email, address, postal_code, name, phone) VALUES (?, ?, ?, ?, ?)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, customer.getEmail());
            ps.setString(2, customer.getAddress());
            ps.setInt(3, customer.getPostalCode());
            ps.setString(4, customer.getName());
            ps.setString(5, customer.getPhone());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Customer could not be inserted, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    customer.setCustomerId(generatedId);
                    return customer;
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new SQLException("Error inserting customer: " + e.getMessage(), e);
        }
    }

}
