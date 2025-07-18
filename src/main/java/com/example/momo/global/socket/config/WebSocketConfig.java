package com.example.momo.global.socket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.momo.global.socket.handler.WebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 설정 클래스입니다.
 * Spring 에서 WebSocket 서버를 활성화하고, 특정 엔드포인트(`/ws`)에 WebSocket 핸들러를 등록합니다.
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
	private final WebSocketHandler handler;

	/**
	 * WebSocket 핸들러를 특정 엔드포인트에 등록합니다.
	 * 클라이언트는 /ws 경로로 WebSocket 연결을 시도할 수 있습니다.
	 *
	 * @param registry WebSocket 핸들러를 등록할 수 있는 레지스트리 객체
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		log.info("WebSocket 연결 시작");
		registry.addHandler(handler, "/ws")
			.setAllowedOriginPatterns("*");
	}
}
