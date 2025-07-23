package com.example.momo.global.security.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.momo.domain.auth.domain.dto.AuthUser;
import com.example.momo.domain.auth.exception.AuthException;
import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		// Access 토큰 쿠키가 있는지 확인하고 있다면 해당 쿠키를 지우고 Header에 넣어준다.
		String accessToken = extractAndRemoveAccessTokenCookie(request, response);

		accessToken = accessToken == null ? request.getHeader(HttpHeaders.AUTHORIZATION) : accessToken;

		// Authorization 해더 값이 없으면 다음 filter로 넘김
		// accessToken의 접두사가 "Bearer " 도 아니고 "Bearer_"도 아니면 Access 토큰이 없는 것으로 간주
		if (!StringUtils.hasText(accessToken) || !accessToken.startsWith(JwtTokenProvider.tokenPrefix)) {
			log.info("토큰이 존재하지 않습니다.");
			filterChain.doFilter(request, response);
			return;
		}
		try {

			// 앞에 Bearer 부분 제거 후 순수 토큰만 획득
			accessToken = jwtTokenProvider.subStringToken(accessToken);

			if (!jwtTokenProvider.getCategory(accessToken).equals("access")) {
				throw new AuthException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access 토큰입니다.");
			}

			Long userId = jwtTokenProvider.getUserId(accessToken);
			String role = jwtTokenProvider.getRole(accessToken);

			AuthUser authUser = new AuthUser(userId);

			// 스프링 시큐리티 인증 토큰 생성
			Authentication authToken = new UsernamePasswordAuthenticationToken(
				authUser,
				"",
				List.of(new SimpleGrantedAuthority("ROLE_" + role))
			);

			//ContextHolder 내부 세션에 사용자 등록
			SecurityContextHolder.getContext().setAuthentication(authToken);

			filterChain.doFilter(request, response);
		} catch (AuthException e) {
			log.error("JWT 예외", e);
			response.setStatus(e.getStatusCode().value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(e.getReason(), null)));

		}
	}

	private String extractAndRemoveAccessTokenCookie(HttpServletRequest request, HttpServletResponse response) {
		if (request.getCookies() == null)
			return null;

		String accessToken = null;
		// 쿠키에서 Access 토큰이 있는지 확인하고 있다면 제거 후 응답 헤더에 넣어준다.
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals(HttpHeaders.AUTHORIZATION)) {
				// Access 토큰 쿠키를 지운다.
				accessToken = cookie.getValue();
				Cookie deleteCookie = new Cookie(HttpHeaders.AUTHORIZATION, null);
				deleteCookie.setPath("/");
				deleteCookie.setMaxAge(0);
				response.addCookie(deleteCookie);

				//응답 헤더에 access 토큰을 추가한다.
				accessToken = JwtTokenProvider.tokenPrefix + jwtTokenProvider.subStringToken(accessToken);
				response.addHeader(HttpHeaders.AUTHORIZATION, accessToken);
			}
		}
		return accessToken;
	}
}
