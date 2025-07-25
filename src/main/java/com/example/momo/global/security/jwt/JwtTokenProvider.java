package com.example.momo.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.momo.domain.auth.exception.AuthException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j(topic = "jwtUtil")
public class JwtTokenProvider {

	public static final long ACCESS_TOKEN_EXPIRE_TIME_MS = 10 * 60 * 1000L; //10분
	public static final long REFRESH_TOKEN_EXPIRE_TIME_MS = 24 * 60 * 60 * 1000L; //24시간
	public static final long REFRESH_TOKEN_EXPIRE_TIME_S = 24 * 60 * 60;

	@Value("${jwt.secret.key}")
	private String secret;
	private SecretKey secretKey;
	private final String signatureAlgorithm = Jwts.SIG.HS256.key().build().getAlgorithm();
	public static final String tokenPrefix = "Bearer ";
	public static final String cookieTokenPrefix = "Bearer_";

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
			throw new AuthException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	public String createToken(String category, Long userId, String role, Long expireMs) {

		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim("category", category)
			.claim("role", role)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expireMs))
			.signWith(secretKey)
			.compact();
	}

	public String getCategory(String token) {
		return getClaim(token).get("category", String.class);
	}

	public Long getUserId(String token) {
		return Long.parseLong(getClaim(token).getSubject());
	}

	public String getRole(String token) {
		return getClaim(token).get("role", String.class);
	}

	public String subStringToken(String rawToken) {
		if (!StringUtils.hasText(rawToken)) {
			throw new AuthException(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다.");
		}
		if (!rawToken.startsWith(tokenPrefix) && !rawToken.startsWith(cookieTokenPrefix)) {
			throw new AuthException(HttpStatus.UNAUTHORIZED, "잘못된 토큰 형식입니다.");
		}
		return rawToken.substring(tokenPrefix.length());
	}

	private Claims getClaim(String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
		} catch (ExpiredJwtException e) {
			throw new AuthException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.");
		} catch (SecurityException | MalformedJwtException e) {
			throw new AuthException(HttpStatus.UNAUTHORIZED, "잘못된 JWT 서명입니다.");
		} catch (UnsupportedJwtException e) {
			throw new AuthException(HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰입니다.");
		} catch (IllegalArgumentException e) {
			throw new AuthException(HttpStatus.UNAUTHORIZED, "JWT 토큰이 잘못되었습니다.");
		}
	}

	public ResponseCookie createRefreshTokenCookie(String refresh) {

		return ResponseCookie.from("refresh", refresh)
			.httpOnly(true)  // XSS 공격을 막기 위해서 Http Only 설정을 해준다.
			//                .secure(true) // https 통신 사용할 때 설정
			.maxAge(24 * 60 * 60) //refresh 토큰의 만료시간과 동일하게 설정
			.path("/api/v1/auth/reissue")
			.build();
	}

	public ResponseCookie createAccessTokenCookie(String access) {
		return ResponseCookie.from("Authorization", cookieTokenPrefix + access)
			.httpOnly(true)  // XSS 공격을 막기 위해서 Http Only 설정을 해준다.
			//                .secure(true) // https 통신 사용할 때 설정
			.maxAge(10 * 60) //access 토큰의 만료시간과 동일하게 설정
			.path("/")
			.build();
	}

}
