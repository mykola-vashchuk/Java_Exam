package ua.ukma.edu.exceptions;

public class AppException extends RuntimeException {
    public AppException(String message, Throwable e) { super(message, e); }
    public AppException(String message) {
        super(message);
    }
}
