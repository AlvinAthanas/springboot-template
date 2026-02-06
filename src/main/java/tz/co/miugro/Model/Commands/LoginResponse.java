package co.tz.sheriaconnectapi.Model.Commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import co.tz.sheriaconnectapi.Model.DTOs.UserDTO;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String access;
    private boolean success;
    private UserDTO user;
}
