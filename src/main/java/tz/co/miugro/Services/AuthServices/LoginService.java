package tz.co.miugro.Services.AuthServices;


import tz.co.miugro.Exceptions.InvalidClientTypeException;
import tz.co.miugro.Model.Commands.MobileLoginResponse;
import tz.co.miugro.Model.DTOs.LoginInput;
import tz.co.miugro.Repositories.RefreshTokenRepository;
import tz.co.miugro.Security.Jwt.ClientType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tz.co.miugro.Abstractions.Command;
import tz.co.miugro.Model.Commands.LoginResponse;
import tz.co.miugro.Model.DTOs.UserDTO;
import tz.co.miugro.Model.Entities.User;
import tz.co.miugro.Repositories.UserRepository;
import tz.co.miugro.Security.Jwt.JwtUtil;

@Service
public class LoginService implements Command<LoginInput, LoginResponse> {

    private final AuthenticationManager manager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginService(
            AuthenticationManager manager,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.manager = manager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
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
            } catch (IllegalArgumentException e) {
                throw new InvalidClientTypeException();
            }
        }


        String accessToken = JwtUtil.generateAccessToken(userDetails, clientType);
        String refreshToken = JwtUtil.generateRefreshToken(userDetails, clientType);

        User userEntity = userRepository.findByEmail(loginInput.getUserLoginDTO().getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshService.storeNewRefreshToken(userEntity, clientType, refreshToken, refreshTokenRepository);

        UserDTO userDTO = new UserDTO(userEntity);

        // 🌐 WEB → cookie
        if (clientType == ClientType.WEB) {

            LoginResponse response = new LoginResponse(
                    "Login successful, you're using web!",
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
                "Login successful, you're using mobile!",
                accessToken,
                true,
                userDTO,
                refreshToken
        );

        return ResponseEntity.ok(response);
    }




}
