package com.example.momo.global.utils;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 핸드셰이크 시 Authorization 헤더에 포함된 JWT를 검증하고,
 * 유효한 경우 사용자 ID를 추출하여 WebSocketSession의 attributes에 저장하는 인터셉터입니다.
 * 이후 WebSocketHandler에서 사용자 식별용으로 활용됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
	private final JwtUtil jwtUtil;

	/**
	 * WebSocket 핸드셰이크 전에 호출되어 JWT를 검증하고 사용자 ID를 추출하여 세션에 저장합니다.
	 * 인증 실패 시 WebSocket 연결을 거부합니다.
	 *
	 * @param request    클라이언트의 HTTP 요청
	 * @param response   서버의 HTTP 응답
	 * @param wsHandler  WebSocket 핸들러
	 * @param attributes WebSocketSession 에 전달될 속성 저장소
	 * @return true: 연결 허용 / false: 연결 거부
	 */
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Map<String, Object> attributes) {
		if (request instanceof ServletServerHttpRequest servletRequest) {
			String token = servletRequest.getServletRequest().getHeader("Authorization");

			if (token != null && token.startsWith("Bearer ")) {
				token = token.substring(7);
				try {
					Long userId = jwtUtil.getUserId(token);
					attributes.put("userId", userId);
					return true;
				} catch (Exception e) {
					log.warn("JWT 해석 실패: {}", e.getMessage());
				}
			}
		}
		return false; // 인증 실패 시 연결 거부
	}

	/**
	 * WebSocket 핸드셰이크 이후 호출됩니다.
	 * 현재는 별도의 처리를 하지 않으며, 확장 시 오버라이딩 가능합니다.
	 *
	 * @param request   클라이언트의 HTTP 요청
	 * @param response  서버의 HTTP 응답
	 * @param wsHandler WebSocket 핸들러
	 * @param exception 핸드셰이크 중 발생한 예외 (있을 경우)
	 */
	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {

	}
}
