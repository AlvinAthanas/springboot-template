package tz.co.miugro.Controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tz.co.miugro.Model.Commands.LoginResponse;
import tz.co.miugro.Model.DTOs.UserLoginDTO;
import tz.co.miugro.Services.LoginService.LoginService;

@RestController
@CrossOrigin(origins = "http://localhost", allowedHeaders = {"Authorization", "Content-Type"})
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginDTO loginDTO) {
        return loginService.execute(loginDTO);
    }

}
