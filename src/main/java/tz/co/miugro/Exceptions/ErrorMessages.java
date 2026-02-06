package co.tz.sheriaconnectapi.Exceptions;

import lombok.Getter;

@Getter
public enum ErrorMessages {
    MEMBER_NOT_FOUND("Member not found"),
    ROLE_NOT_FOUND("Role not found"),
    AUTHORITY_NOT_FOUND("Authority not found"),
    EMAIL_ALREADY_EXISTS("Email already exists"),
    INVALID_CLIENT_TYPE("Invalid client type"),
    ;

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

}
