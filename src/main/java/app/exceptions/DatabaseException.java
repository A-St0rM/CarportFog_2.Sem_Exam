package app.exceptions;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String userMessage) {
        super(userMessage);
        System.out.println("userMessage: " + userMessage);
    }

    public DatabaseException(String userMessage, String systemMessage) {
        super(userMessage);
        System.out.println("errorMessage: " + userMessage);
        System.out.println("systemMessage: " + systemMessage);
    }
}
