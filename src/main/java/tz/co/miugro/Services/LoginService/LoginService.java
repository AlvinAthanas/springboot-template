package tz.co.miugro.Services.LoginService;


import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tz.co.miugro.Abstractions.Command;
import tz.co.miugro.Exceptions.UserNotFoundException;
import tz.co.miugro.Model.Commands.LoginResponse;
import tz.co.miugro.Model.DTOs.UserDTO;
import tz.co.miugro.Model.DTOs.UserLoginDTO;
import tz.co.miugro.Model.Entities.User;
import tz.co.miugro.Repositories.UserRepository;
import tz.co.miugro.Security.Jwt.JwtUtil;

import java.util.Optional;

@Service
public class LoginService implements Command<UserLoginDTO, LoginResponse> {

    private final AuthenticationManager manager;
    private final UserRepository userRepository;

    public LoginService(AuthenticationManager manager, UserRepository userRepository) {
        this.manager = manager;
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<LoginResponse> execute(UserLoginDTO input) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword());

            Authentication authentication = manager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            assert userDetails != null;
            String accessToken = JwtUtil.generateAccessToken(userDetails);
            String refreshToken = JwtUtil.generateRefreshToken(userDetails);

            // Get user entity and DTO
            tz.co.miugro.Model.Entities.User userEntity = userRepository.findByEmail(input.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            UserDTO userDTO = new UserDTO(userEntity);

            LoginResponse responseBody = new LoginResponse(
                    "Login successfully.",
                    accessToken,
                    true,
                    userDTO
            );

            return ResponseEntity.ok()
                    .header("Set-Cookie", "refresh_token=" + refreshToken
                            + "; HttpOnly; Path=/; Max-Age=" + (7 * 24 * 60 * 60)
                            + "; SameSite=None; Secure")
                    .body(responseBody);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new LoginResponse(
                            "An error occurred: " + e.getMessage(),
                            null,
                            false,
                            null
                    ));
        }
    }


}
