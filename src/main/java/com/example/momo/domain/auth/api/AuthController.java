package com.example.momo.domain.auth.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.auth.application.AuthService;
import com.example.momo.domain.auth.domain.dto.LoginRequestDto;
import com.example.momo.domain.auth.domain.dto.LoginResponseDto;
import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
		LoginResponseDto response = authService.loginUser(request);
		// Access 토큰과 Refresh 토큰 발행
		String access = jwtTokenProvider.createToken("access", response.getId(), "ADMIN",
			JwtTokenProvider.ACCESS_TOKEN_EXPIRE_TIME_MS);
		String refresh = jwtTokenProvider.createToken("refresh", response.getId(), "USER",
			JwtTokenProvider.REFRESH_TOKEN_EXPIRE_TIME_MS);

		return ResponseEntity.status(HttpStatus.OK)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
			.header(HttpHeaders.SET_COOKIE,
				jwtTokenProvider.createRefreshTokenCookie(refresh).toString()) // Refresh 토큰 쿠키 설정
			.body(ApiResponse.success("로그인에 성공했습니다.", response));
	}

	@PostMapping("reissue")
	public ResponseEntity<ApiResponse<Void>> reissueToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = extractedRefreshToken(request);
		// 쿠키에 Refresh 토큰 있는지 확인
		if (refreshToken == null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("Refresh 토큰이 존재하지 않습니다.", null));
		//  Refresh 토큰인지 확인
		if (!jwtTokenProvider.getCategory(refreshToken).equals("refresh"))
			ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("유효하지 않은 토큰입니다.", null));

		Long userId = jwtTokenProvider.getUserId(refreshToken);
		String role = jwtTokenProvider.getRole(refreshToken);

		String accessToken = jwtTokenProvider.createToken("access", userId, role,
			JwtTokenProvider.ACCESS_TOKEN_EXPIRE_TIME_MS);

		return ResponseEntity.status(HttpStatus.OK)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.body(ApiResponse.success("Access 토큰을 재발급했습니다.", null));
	}

	private String extractedRefreshToken(HttpServletRequest request) {
		if (request.getCookies() == null)
			return null;

		String refreshToken = null;
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("refresh")) {
				refreshToken = cookie.getValue();
			}
		}
		return refreshToken;
	}
}
