package tz.co.miugro.Services.AuthServices;

import tz.co.miugro.Abstractions.Command;
import tz.co.miugro.Exceptions.ErrorMessages;
import tz.co.miugro.Exceptions.InvalidTokenException;
import tz.co.miugro.Model.DTOs.RefreshInput;
import tz.co.miugro.Model.DTOs.RefreshTokenRequest;
import tz.co.miugro.Model.Entities.RefreshToken;
import tz.co.miugro.Model.Entities.User;
import tz.co.miugro.Repositories.RefreshTokenRepository;
import tz.co.miugro.Security.Jwt.ClientType;
import tz.co.miugro.Security.Jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RefreshService implements Command<RefreshInput, Map<String, Object>> {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }



    private String extractRefreshToken(
            HttpServletRequest request,
            RefreshTokenRequest body
    ) {
        // 1️⃣ Try cookie (WEB)
        String cookieToken = Arrays.stream(
                        Optional.ofNullable(request.getCookies())
                                .orElse(new Cookie[0])
                )
                .filter(c -> c.getName().equals("refresh_token"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (cookieToken != null) {
            return cookieToken;
        }

        // 2️⃣ Try request body (MOBILE)
        if (body != null && body.getRefreshToken() != null) {
            return body.getRefreshToken();
        }

        return null;
    }

    public static void storeNewRefreshToken(
            User user,
            ClientType clientType,
            String token,
            RefreshTokenRepository refreshTokenRepository) {
        RefreshToken entity = new RefreshToken();
        entity.setToken(token);
        entity.setUser(user);
        entity.setClientType(clientType);
        entity.setExpiryDate(
                Instant.now().plusMillis(
                        clientType == ClientType.MOBILE
                                ? 30L * 24 * 60 * 60 * 1000
                                : 7L * 24 * 60 * 60 * 1000
                )
        );
        refreshTokenRepository.save(entity);
    }

    @Override
    public ResponseEntity<Map<String, Object>> execute(RefreshInput input) {

        // 1️⃣ Extract refresh token (WEB → cookie, MOBILE → body)
        String tokenValue = extractRefreshToken(input.getRequest(), input.getBody());
        if (tokenValue == null) {
            throw new InvalidTokenException(ErrorMessages.MISSING_TOKEN.getMessage());
        }

        // 2️⃣ Validate token existence in DB
        RefreshToken storedToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() ->
                        new InvalidTokenException(ErrorMessages.INVALID_TOKEN.getMessage())
                );

        // 3️⃣ Check expiration / revocation
        if (storedToken.isRevoked()
                || storedToken.getExpiryDate().isBefore(Instant.now())) {

            refreshTokenRepository.deleteAllByUserId(
                    storedToken.getUser().getId()
            );

            throw new InvalidTokenException(ErrorMessages.INVALID_TOKEN.getMessage());
        }

        User user = storedToken.getUser();
        ClientType clientType = storedToken.getClientType();

        // 4️⃣ ROTATE: revoke old refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // 5️⃣ Build authorities
        var authorities = user.getRoles().stream()
                .flatMap(role -> role.getAuthorities().stream())
                .map(a -> new SimpleGrantedAuthority(a.getName()))
                .collect(Collectors.toSet());

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .build();

        // 6️⃣ Generate new tokens
        String newAccessToken = JwtUtil.generateAccessToken(userDetails, clientType);
        String newRefreshToken = JwtUtil.generateRefreshToken(userDetails, clientType);

        storeNewRefreshToken(user, clientType, newRefreshToken, refreshTokenRepository);

        // 7️⃣ WEB → HttpOnly cookie
        if (clientType == ClientType.WEB) {
            Cookie cookie = new Cookie("refresh_token", newRefreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // HTTPS only
            cookie.setPath("/auth/refresh");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            input.getResponse().addCookie(cookie);
        }

        // 8️⃣ Response
        Map<String, Object> bodyResponse = new HashMap<>();
        bodyResponse.put("access", newAccessToken);
        bodyResponse.put("success", true);
        bodyResponse.put(
                "refresh",
                clientType == ClientType.MOBILE ? newRefreshToken : null
        );

        return ResponseEntity.ok(bodyResponse);
    }
}
