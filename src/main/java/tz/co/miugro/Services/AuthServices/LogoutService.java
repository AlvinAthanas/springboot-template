package tz.co.miugro.Services.AuthServices;

import tz.co.miugro.Abstractions.Command;
import tz.co.miugro.Exceptions.UserNotFoundException;
import tz.co.miugro.Model.DTOs.LogoutInput;
import tz.co.miugro.Model.Entities.User;
import tz.co.miugro.Repositories.RefreshTokenRepository;
import tz.co.miugro.Repositories.UserRepository;
import tz.co.miugro.Security.Jwt.ClientType;
import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService implements Command<LogoutInput, Void> {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    @Override
    public ResponseEntity<Void> execute(LogoutInput input) {

        if (input.getAuth() == null || !input.getAuth().isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        System.out.println("Name: "+ input.getAuth().getName());
        System.out.println("Principle: "+input.getAuth().getPrincipal());
        // 1️⃣ Resolve user
        User user = userRepository
                .findByEmail(input.getAuth().getName())
                .orElseThrow(UserNotFoundException::new);

        // 2️⃣ Delete ALL refresh tokens (logout everywhere)
        refreshTokenRepository.deleteAllByUserId(user.getId());

        // 3️⃣ Detect client type (WEB defaults)
        String clientHeader = input.getRequest().getHeader("X-Client-Type");
        ClientType clientType = ClientType.WEB;

        if (clientHeader != null) {
            try {
                clientType = ClientType.valueOf(clientHeader.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        // 4️⃣ WEB → clear HttpOnly refresh cookie
        if (clientType == ClientType.WEB) {
            Cookie cookie = new Cookie("refresh_token", "");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/auth/refresh");
            cookie.setMaxAge(0); // delete
            input.getResponse().addCookie(cookie);
        }

        // 5️⃣ Clear Spring Security context (invalidate access token *effectively*)
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }
}
