package co.tz.sheriaconnectapi.Model.DTOs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginInput {
    private UserLoginDTO  userLoginDTO;
    private HttpServletRequest request;
}
