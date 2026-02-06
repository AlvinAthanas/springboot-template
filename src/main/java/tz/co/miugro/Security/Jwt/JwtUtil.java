package tz.co.miugro.Security.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JwtUtil {

    private static final String SECRET_KEY = "a-string-secret-at-least-256-bits-long";
    private static final long ACCESS_EXPIRATION = 5 * 60 * 1000; // 5 minutes
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    public static String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername(), userDetails.getAuthorities(), ACCESS_EXPIRATION);
    }

    public static String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername(), userDetails.getAuthorities(), REFRESH_EXPIRATION);
    }

    public static String generateToken(String username, Collection<? extends GrantedAuthority> authorities, long expiration) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public static Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean isTokenValid(String token) {
        try {
            return !getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}

