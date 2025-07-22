package com.example.momo.global.socket.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.momo.global.socket.dto.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 연결을 관리하고 사용자별 메시지를 전송하는 핸들러 클래스입니다.
 * 사용자의 WebSocketSession 을 userId 기준으로 저장하고,
 * 서버에서 특정 사용자에게 메시지를 보낼 수 있도록 지원합니다.
 * todo:현재는 session 방식으로 url 에 있는 userId 를 바탕으로 임시 구현 -> JWT 방식으로 변경시 로직 변경 예정
 */

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {
	// userId → session 매핑 저장소
	private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

	//Json 타입 변경
	private final ObjectMapper objectMapper;

	public WebSocketHandler() {
		this.objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	/**
	 * 클라이언트와 WebSocket 연결이 성공적으로 수립되었을 때 호출됩니다.
	 * 핸드셰이크 인터셉터에서 추출해 세션에 저장된 사용자 ID를 가져와,
	 * 해당 사용자와 세션을 매핑에 저장합니다.
	 *
	 * @param session 연결된 클라이언트의 WebSocket 세션
	 */
	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) {
		Long userId = (Long)session.getAttributes().get("userId");
		userSessions.put(userId, session);
	}

	/**
	 * todo : 현재는 사용하지 않지만 추후 사용 가능성 고려하여 생성 -> 미사용시 삭제 예정
	 * 클라이언트로부터 텍스트 메시지를 수신했을 때 호출됩니다.
	 * 수신된 메시지를 그대로 Echo 형식으로 다시 전송하고 로그를 남깁니다.
	 *
	 * @param session 메시지를 보낸 클라이언트 세션
	 * @param message 수신된 텍스트 메시지
	 * @throws IOException 메시지 전송 실패 시 예외 발생
	 */
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
		session.sendMessage(new TextMessage("전달 완료: " + message.getPayload()));
		log.debug("메세지 수신 :: {}", message.getPayload());
	}

	/**
	 * 클라이언트와의 WebSocket 연결이 종료되었을 때 호출됩니다.
	 * 연결이 종료된 세션을 userSessions 맵에서 제거합니다.
	 *
	 * @param session 종료된 세션
	 * @param status  종료 상태 정보
	 */
	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
		userSessions.values().removeIf(s -> s.getId().equals(session.getId()));
		log.info("WebSocket 연결 종료");
	}

	/**
	 * 서버에서 특정 사용자에게 알림 메시지를 전송합니다.
	 * 세션이 유효하고 열려 있는 경우에만 메시지를 전송합니다.
	 * 메시지 전송 중 오류가 발생하면 내부에서 로그를 출력하고 예외는 전파하지 않습니다.
	 *
	 * @param message 전송할 알림 메시지 (userId, content, timestamp 포함)
	 */
	public void sendToUser(NotificationMessage message) {
		WebSocketSession session = userSessions.get(message.userId());
		if (session != null && session.isOpen()) {
			try {
				String json = objectMapper.writeValueAsString(message);
				session.sendMessage(new TextMessage(json));
			} catch (IOException e) {
				log.warn("웹소켓 메시지 전송 실패: userId={}, error={}", message.userId(), e.getMessage());
			}
		}
	}
}
