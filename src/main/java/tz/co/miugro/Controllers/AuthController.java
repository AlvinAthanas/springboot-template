package tz.co.miugro.Controllers;

import tz.co.miugro.Model.Commands.LoginResponse;
import tz.co.miugro.Model.DTOs.*;
import tz.co.miugro.Services.AuthServices.LoginService;
import tz.co.miugro.Services.AuthServices.LogoutService;
import tz.co.miugro.Services.AuthServices.RefreshService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginService loginService;
    private final RefreshService refreshService;
    private final LogoutService logoutService;

    public AuthController(
            LoginService loginService,
            RefreshService refreshService,
            LogoutService logoutService
    ) {
        this.loginService = loginService;
        this.refreshService = refreshService;
        this.logoutService = logoutService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody UserLoginDTO dto,
            HttpServletRequest request
    ) {
        return loginService.execute(new LoginInput(dto, request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return refreshService.execute(
                new RefreshInput(request, response, body)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            Authentication auth,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return logoutService.execute(
                new LogoutInput(auth, request, response)
        );
    }

}

