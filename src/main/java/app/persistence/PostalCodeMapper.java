package app.persistence;

import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostalCodeMapper {

    private final ConnectionPool connectionPool;

    public PostalCodeMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public String getCityByPostalCode(int postalCode) throws DatabaseException {
        String sql = "SELECT city FROM postal_codes WHERE postal_code = ?";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postalCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("city");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved opslag af by for postnummer: " + e.getMessage());
        }
    }


}
