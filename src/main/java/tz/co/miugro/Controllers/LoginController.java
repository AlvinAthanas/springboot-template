package tz.co.miugro.Controllers;


import tz.co.miugro.Model.Commands.LoginResponse;
import tz.co.miugro.Model.DTOs.LoginInput;
import tz.co.miugro.Model.DTOs.UserLoginDTO;
import tz.co.miugro.Services.AuthServices.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginDTO loginDTO, HttpServletRequest request) {
        return loginService.execute(new LoginInput(loginDTO, request));
    }

}
