package co.tz.sheriaconnectapi.Services.LoginService;


import co.tz.sheriaconnectapi.Model.Commands.MobileLoginResponse;
import co.tz.sheriaconnectapi.Model.DTOs.LoginInput;
import co.tz.sheriaconnectapi.Security.Jwt.ClientType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import co.tz.sheriaconnectapi.Abstractions.Command;
import co.tz.sheriaconnectapi.Exceptions.UserNotFoundException;
import co.tz.sheriaconnectapi.Model.Commands.LoginResponse;
import co.tz.sheriaconnectapi.Model.DTOs.UserDTO;
import co.tz.sheriaconnectapi.Model.DTOs.UserLoginDTO;
import co.tz.sheriaconnectapi.Model.Entities.User;
import co.tz.sheriaconnectapi.Repositories.UserRepository;
import co.tz.sheriaconnectapi.Security.Jwt.JwtUtil;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

@Service
public class LoginService implements Command<LoginInput, LoginResponse> {

    private final AuthenticationManager manager;
    private final UserRepository userRepository;

    public LoginService(AuthenticationManager manager, UserRepository userRepository) {
        this.manager = manager;
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<LoginResponse> execute(LoginInput loginInput) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        loginInput.getUserLoginDTO().getEmail(),
                        loginInput.getUserLoginDTO().getPassword()
                );

        Authentication authentication = manager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // ✅ Proper client detection
        String clientHeader = loginInput.getRequest().getHeader("X-Client-Type");
        ClientType clientType = ClientType.WEB;

        if (clientHeader != null) {
            try {
                clientType = ClientType.valueOf(clientHeader.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        String accessToken = JwtUtil.generateAccessToken(userDetails, clientType);
        String refreshToken = JwtUtil.generateRefreshToken(userDetails, clientType);

        User userEntity = userRepository.findByEmail(loginInput.getUserLoginDTO().getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO userDTO = new UserDTO(userEntity);

        // 🌐 WEB → cookie
        if (clientType == ClientType.WEB) {

            LoginResponse response = new LoginResponse(
                    "Login successful",
                    accessToken,
                    true,
                    userDTO
            );

            return ResponseEntity.ok()
                    .header(
                            "Set-Cookie",
                            "refresh_token=" + refreshToken +
                                    "; HttpOnly; Path=/auth/refresh" +
                                    "; Max-Age=" + (7 * 24 * 60 * 60) +
                                    "; SameSite=None; Secure"
                    )
                    .body(response);
        }

        // 📱 MOBILE → response body
        MobileLoginResponse response = new MobileLoginResponse(
                "Login successful",
                accessToken,
                true,
                userDTO,
                refreshToken
        );

        return ResponseEntity.ok(response);
    }




}
