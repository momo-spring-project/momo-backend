package com.example.momo.global.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j(topic = "jwtUtil")
public class JwtUtil {
    @Value("${jwt.secret.key}")
    private String secret;
    private SecretKey secretKey;
    private final String signatureAlgorithm = Jwts.SIG.HS256.key().build().getAlgorithm();
    private static final String tokenPrefix = "Bearer ";
    @PostConstruct
    public void init() {
        try {
            if (!StringUtils.hasText(secret)) {
                throw new IllegalArgumentException("JWT 시크릿 키가 설정되지 않았습니다.");
            }

            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalArgumentException("JWT 시크릿 키는 최소 32바이트 이상이어야 합니다.");
            }

            secretKey = new SecretKeySpec(bytes, signatureAlgorithm);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public String createToken(Long userId, String email, String nickname) {
        return "Bearer "+ Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("nickname", nickname)
                .claim("role", "USER")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 86400000L))
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaim(token).getSubject());
    }

    public String getEmail(String token) {
        return getClaim(token).get("email", String.class);
    }

    public String getNickname(String token) {
        return getClaim(token).get("nickname", String.class);
    }

    public String getRole(String token) {
        return getClaim(token).get("role", String.class);
    }

    public String subStringToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다.");
        }
        if (!rawToken.startsWith(tokenPrefix)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "잘못된 토큰 형식입니다.");
        }
        return rawToken.split(" ")[1];
    }

    private Claims getClaim(String token) {
        try{
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.");
        } catch (SecurityException | MalformedJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "잘못된 JWT 서명입니다.");
        } catch (UnsupportedJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 토큰이 잘못되었습니다.");
        }
    }

}
