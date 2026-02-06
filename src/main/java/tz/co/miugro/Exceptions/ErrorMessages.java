package tz.co.miugro.Exceptions;

public enum ErrorMessages {
    MEMBER_NOT_FOUND("Member not found"),
    ROLE_NOT_FOUND("Role not found"),
    AUTHORITY_NOT_FOUND("Authority not found"),
    EMAIL_ALREADY_EXISTS("Email already exists"),
    ;

    private String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
