package app.persistence;

import app.entities.Customer;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static app.Main.connectionPool;

public class CustomerMapper {

    public CustomerMapper(ConnectionPool connectionPool) {
    }

    public static List<Customer> getCustomersByCustomerId(ConnectionPool connectionPool, int customerId) throws DatabaseException {

        List<Customer> customerList = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (
                PreparedStatement preparedStatement = connectionPool.getConnection().prepareStatement(sql)
        ) {
            preparedStatement.setInt(1, customerId);
            var rs = preparedStatement.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String city = rs.getString("city");
                int postalCode = rs.getInt("postalCode");

                Customer customer;
                customer = new Customer (email,address,phone,name,postalCode);
                customerList.add(customer);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return customerList;
    }

    public void createCustomer(String email, String address, String phone, String name, int postalCode ) throws SQLException {
        String sql = "INSERT INTO customers (email, address, phone, name, postalCode) VALUES (?, ?, ?, ?, ?)";

        try(Connection connection = connectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, email);
            ps.setString(2, address);
            ps.setString(3, phone);
            ps.setString(4, name);
            ps.setInt(5, postalCode);
            ps.executeUpdate();

        }catch (SQLException e) {
            String msg = e.getMessage();
        }

    }
}
