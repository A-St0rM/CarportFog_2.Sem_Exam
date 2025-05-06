package app.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Hashes a plain text password using BCrypt.
     * @param plainPassword The password to hash.
     * @return The generated salt and hash combination.
     */
    public static String hashPassword(String plainPassword) {
        // gensalt automatically includes work factor (default 10, 12 is stronger/slower)
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Checks if a plain text password matches a stored BCrypt hash.
     * @param plainPassword The password attempt from the user.
     * @param hashedPasswordFromDB The hash stored in the database.
     * @return true if the password matches the hash, false otherwise.
     */
    public static boolean checkPassword(String plainPassword, String hashedPasswordFromDB) {
        if (plainPassword == null || hashedPasswordFromDB == null || hashedPasswordFromDB.isEmpty()) {
            return false; // Avoid errors
        }
        try {
            // BCrypt.checkpw handles extracting the salt from the stored hash
            return BCrypt.checkpw(plainPassword, hashedPasswordFromDB);
        } catch (IllegalArgumentException e) {
            // This can happen if the stored hash is not in the expected BCrypt format
            System.err.println("PasswordUtil Warning: Invalid hash format provided to checkPassword: " + e.getMessage());
            return false;
        }
    }
}