package app.persistence;

import app.DTO.AdminDTO;
// import app.entities.Admin; // Not strictly needed for these methods
import app.exceptions.DatabaseException;
import app.util.PasswordUtil; // <<< IMPORT PASSWORD UTIL

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminMapper {

    private final ConnectionPool connectionPool;

    public AdminMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public AdminDTO login(String email, String plainPassword) throws DatabaseException, SQLException {
        // SQL selects the user based on email (case-insensitive) to get the stored hash
        String sql = "SELECT admin_id, email, password FROM admins WHERE LOWER(email) = LOWER(?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashedPasswordFromDB = rs.getString("password");
                int id = rs.getInt("admin_id");
                String adminEmail = rs.getString("email");

                // Check if the provided plain password matches the stored hash
                boolean passwordMatch = PasswordUtil.checkPassword(plainPassword, hashedPasswordFromDB);

                if (passwordMatch) {
                    return new AdminDTO(id, adminEmail);
                } else {
                    throw new DatabaseException("Forkert kodeord.");
                }
            } else {
                throw new DatabaseException("Email er ikke oprettet.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Databasefejl under login.", e.getMessage());
        }
    }

    public void createAdmin(String email, String hashedPassword) throws DatabaseException {
        String sql = "INSERT INTO admins (email, password) VALUES (?,?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, email);
            ps.setString(2, hashedPassword); // Store the HASHED password

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Ingen rækker blev oprettet. Prøv igen.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Der er sket en fejl under oprettelse. Prøv igen ", e.getMessage());
        }
    }
}