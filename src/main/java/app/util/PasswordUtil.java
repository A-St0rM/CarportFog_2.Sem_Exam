package app.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {


     // Hashes a plain text password using BCrypt.
    public static String hashPassword(String plainPassword) {
        // gensalt automatically includes work factor (default is 10 but 12 is stronger because it is slower and therefore harder to bruteforce.
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(plainPassword, salt);
    }

     //Checks if a plain text password matches a stored BCrypt hash.
    public static boolean checkPassword(String plainPassword, String hashedPasswordFromDB) {
        if (plainPassword == null || hashedPasswordFromDB == null || hashedPasswordFromDB.isEmpty()) {
            return false; // to Avoid errors
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