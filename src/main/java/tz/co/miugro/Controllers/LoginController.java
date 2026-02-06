package co.tz.sheriaconnectapi.Controllers;


import co.tz.sheriaconnectapi.Model.DTOs.LoginInput;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.tz.sheriaconnectapi.Model.Commands.LoginResponse;
import co.tz.sheriaconnectapi.Model.DTOs.UserLoginDTO;
import co.tz.sheriaconnectapi.Services.LoginService.LoginService;

@RestController
@CrossOrigin(origins = "http://localhost", allowedHeaders = {"Authorization", "Content-Type"})
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
