package com.example.momo.global.sse.application;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.momo.global.socket.dto.WebSocketNotificationDto;

import lombok.extern.slf4j.Slf4j;

//SSE 기본구조. 현재는 사용 X. 추후 사용 고려
@Slf4j
@Service
public class SseServiceImpl implements SseService {

	// 사용자별 emitter 저장소
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	@Override
	public SseEmitter connect(Long userId) {
		// 1분간 타임아웃 설정
		SseEmitter emitter = new SseEmitter(60 * 1000L);
		emitters.put(userId, emitter);

		// 연결 종료/오류 시 emitter 제거
		emitter.onCompletion(() -> emitters.remove(userId));
		emitter.onTimeout(() -> emitters.remove(userId));
		emitter.onError(e -> emitters.remove(userId));

		return emitter;
	}

	@Override
	public void send(WebSocketNotificationDto message) {
		SseEmitter emitter = emitters.get(message.getUserId());
		if (emitter != null) {
			try {

				emitter.send(SseEmitter.event()
					.name("notification")
					.data(message));
				log.debug("SSE 메세지 전송 성공: userId={}, content={}", message.getUserId(), message.getContent());
			} catch (IOException e) {
				// 전송 실패 시 emitter 제거
				log.warn("SSE 메세지 전송 실패: userId={}, content={}, error={}", message.getUserId(), message.getContent(),
					e.toString());
				emitters.remove(message.getUserId());
			}
		}
	}
}
