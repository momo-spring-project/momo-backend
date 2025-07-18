package com.example.momo.global.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
	// userId → session 매핑
	private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		Long userId = extractUserIdFromQuery(session); // 유저 ID를 queryParam 등에서 파싱
		userSessions.put(userId, session);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
		session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		userSessions.values().removeIf(s -> s.getId().equals(session.getId()));
	}

	public void sendToUser(Long userId, String message) throws IOException {
		WebSocketSession session = userSessions.get(userId);
		if (session != null && session.isOpen()) {
			session.sendMessage(new TextMessage(message));
		}
	}

	private Long extractUserIdFromQuery(WebSocketSession session) {
		// 예: ws://localhost:8080/ws?userId=42
		String query = session.getUri().getQuery(); // "userId=42"
		return Long.parseLong(query.split("=")[1]);
	}
}
