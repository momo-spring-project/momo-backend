package com.example.momo.domain.notification.application.sse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.momo.domain.notification.application.sse.dto.SseMessageDto;

import lombok.extern.slf4j.Slf4j;

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
	public boolean sendIfConnected(SseMessageDto responseDto) {
		SseEmitter emitter = emitters.get(responseDto.getUserId());
		if (emitter != null) {
			try {

				emitter.send(SseEmitter.event()
					.name("notification")
					.data(responseDto));
				log.debug("SSE 메세지 전송 성공: userId={}, content={}", responseDto.getUserId(), responseDto.getContent());
				return true;
			} catch (Exception e) {
				// 전송 실패 시 emitter 제거
				log.warn("SSE 메세지 전송 실패: userId={}, content={}, error={}", responseDto.getUserId(),
					responseDto.getContent(),
					e.toString());
				emitters.remove(responseDto.getUserId());
				return false;
			}
		}
		log.warn("SSE 메세지 연결 실패: userId={}, content={}", responseDto.getUserId(), responseDto.getContent());
		return false;
	}
}
