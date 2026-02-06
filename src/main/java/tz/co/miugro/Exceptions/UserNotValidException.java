package co.tz.sheriaconnectapi.Exceptions;

public class UserNotValidException extends RuntimeException {
    public UserNotValidException(String message) {
        super(message);
    }
}
