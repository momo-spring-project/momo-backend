package com.example.momo.global.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.global.utils.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final JwtUtil jwtUtil;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		// SecurityContext에서 authUser 꺼냄
		AuthUser authUser = (AuthUser)authentication.getPrincipal();

		// Access 토큰과 Refresh 토큰을 생성
		String access = jwtUtil.createToken("access", authUser.getId(), "USER", JwtUtil.ACCESS_TOKEN_EXPIRE_TIME_MS);
		String refresh = jwtUtil.createToken("refresh", authUser.getId(), "USER", JwtUtil.REFRESH_TOKEN_EXPIRE_TIME_MS);

		// Access 토큰과 Refresh 토큰을 쿠키로 만들어서 응답애 담는다.
		// ps. 프론트엔드에서는 하이퍼링크 형태로 OAuth2 주소를 클라이언트들에게 제공하기 때문에 토큰을 프론트에서 받기 위해선 쿠키로 응답해야함.
		response.addHeader(HttpHeaders.SET_COOKIE, jwtUtil.createRefreshTokenCookie(refresh).toString());
		response.addHeader(HttpHeaders.SET_COOKIE, jwtUtil.createAccessTokenCookie(access).toString());
		response.sendRedirect("http://localhost:3000/");
	}
}
