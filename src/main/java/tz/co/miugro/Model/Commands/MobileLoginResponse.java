package co.tz.sheriaconnectapi.Model.Commands;

import lombok.Getter;
import co.tz.sheriaconnectapi.Model.DTOs.UserDTO;

@Getter
public class MobileLoginResponse extends LoginResponse {

    private final String refreshToken;

    public MobileLoginResponse(
            String message,
            String access,
            boolean success,
            UserDTO user,
            String refreshToken
    ) {
        super(message, access, success, user);
        this.refreshToken = refreshToken;
    }
}
