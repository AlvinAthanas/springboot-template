package tz.co.miugro.Model.Commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tz.co.miugro.Model.DTOs.UserDTO;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String access;
    private boolean success;
    private UserDTO user;
}
