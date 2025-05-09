package app.exceptions;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, String eMessage) {
        super(message);
    }
}
