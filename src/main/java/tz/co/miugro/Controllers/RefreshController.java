package co.tz.sheriaconnectapi.Controllers;

import co.tz.sheriaconnectapi.Security.Jwt.ClientType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.tz.sheriaconnectapi.Model.Entities.User;
import co.tz.sheriaconnectapi.Repositories.UserRepository;
import co.tz.sheriaconnectapi.Security.Jwt.JwtUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class RefreshController {

    private final UserRepository userRepository;

    public RefreshController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> cookie.getName().equals("refresh_token"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null || !JwtUtil.isTokenValid(refreshToken)) {
            // Delete invalid refresh token cookie
            Cookie deleteCookie = new Cookie("refresh_token", null);
            deleteCookie.setHttpOnly(true);
            deleteCookie.setPath("/");
            deleteCookie.setMaxAge(0);
            response.addCookie(deleteCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("access", null);
            responseBody.put("success", false);
            responseBody.put("message", "Invalid or missing refresh token");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);

        }

        String email = JwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("User roles: " + user.getRoles());
        System.out.println("Authorities: " + user.getRoles().stream()
                .map(r -> "ROLE_" + r.getName())
                .toList());
        String clientTypeHeader = request.getHeader("X-Client-Type");
        ClientType clientType = ClientType.WEB; // default

        if (clientTypeHeader != null) {
            try {
                clientType = ClientType.valueOf(clientTypeHeader.toUpperCase());
            } catch (IllegalArgumentException e) {
                // optional: log warning about invalid client type
            }
        }

        // Generate new access token
        String newAccessToken = JwtUtil.generateAccessToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities(user.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                                .collect(Collectors.toList()))
                        .build(),
                clientType
        );
        System.out.println("Generated access token: " + newAccessToken);


        // Reset refresh token cookie (optional, can refresh expiration)
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        cookie.setSecure(false); // true in production with HTTPS
        response.addCookie(cookie);

        Map<String, Object> body = new HashMap<>();
        body.put("access", newAccessToken);
        body.put("success", true);
        body.put("message", "Token refreshed successfully");

        return ResponseEntity.ok(body);

    }
}
