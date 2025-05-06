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

    /**
     * Attempts to log in an admin by verifying the provided password against the stored hash.
     * @param email The email entered by the user.
     * @param plainPassword The plain text password entered by the user.
     * @return AdminDTO containing id and email if login is successful.
     * @throws DatabaseException If login fails (email not found or password incorrect).
     * @throws SQLException If a database error occurs.
     */
    public AdminDTO login(String email, String plainPassword) throws DatabaseException, SQLException {
        // SQL selects the user based on email (case-insensitive) to get the stored hash
        String sql = "SELECT admin_id, email, password FROM admins WHERE LOWER(email) = LOWER(?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email); // Set only the email parameter
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // if user with that email exists, get the stored hash
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
                throw new DatabaseException("Email findes ikke.");
            }
        } catch (SQLException e) {

            System.err.println("SQL Error during admin login: " + e.getMessage());
            throw new DatabaseException("Databasefejl under login.", e.getMessage());
        }
    }

    /**
     * Creates a new admin user in the database with an already hashed password.
     * @param email The email for the new admin.
     * @param hashedPassword The BCrypt hashed password.
     * @throws DatabaseException If the user could not be created (e.g., email already exists).
     */
    public void createAdmin(String email, String hashedPassword) throws DatabaseException {
        String sql = "INSERT INTO admins (email, password) VALUES (?,?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, email);
            ps.setString(2, hashedPassword); // Store the HASHED password

            int rowsAffected = ps.executeUpdate();
        } catch (SQLException e) {
            String msg = "Der er sket en fejl under oprettelse. Prøv igen";
            if ("23505".equals(e.getSQLState())) { // Check for unique violation SQLState
                msg = "Email findes allerede. Brug en anden.";
            } else {
                System.err.println("SQL Error during admin creation: " + e.getMessage() + ", SQLState: " + e.getSQLState());
            }
            throw new DatabaseException(msg, e.getMessage());
        }
    }
}