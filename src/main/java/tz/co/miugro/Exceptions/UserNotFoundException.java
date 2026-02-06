package tz.co.miugro.Exceptions;

public class UserNotFoundException  extends RuntimeException{
    public UserNotFoundException() {
        super(ErrorMessages.MEMBER_NOT_FOUND.getMessage());
    }
}
