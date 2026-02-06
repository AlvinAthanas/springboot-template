package tz.co.miugro.Model.DTOs;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserLoginDTO {
    private String email;
    private String password;

    public UserLoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
