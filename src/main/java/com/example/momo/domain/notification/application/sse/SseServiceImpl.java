package com.example.momo.domain.notification.application.sse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.momo.domain.notification.application.sse.dto.SseMessageDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SseServiceImpl implements SseService {

	// 사용자별 emitter 저장소
	private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	@Override
	public SseEmitter connect(Long userId) {
		// 1분간 타임아웃 설정
		SseEmitter emitter = new SseEmitter(60 * 1000L);
		emitter.onCompletion(() -> removeEmitter(userId, emitter));
		emitter.onTimeout(() -> removeEmitter(userId, emitter));
		emitter.onError(e -> removeEmitter(userId, emitter));

		emitters.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

		return emitter;
	}

	@Override
	public boolean sendIfConnected(SseMessageDto dto) {
		List<SseEmitter> userEmitters = emitters.get(dto.getUserId());
		if (userEmitters == null || userEmitters.isEmpty()) {
			log.warn("SSE 메세지 연결 실패: userId={}, content={}", dto.getUserId(), dto.getContent());
			return false;
		}

		boolean atLeastOneSuccess = false;

		for (SseEmitter emitter : userEmitters) {
			try {
				emitter.send(SseEmitter.event()
					.name("notification")
					.data(dto));
				atLeastOneSuccess = true;
			} catch (Exception e) {
				log.warn("SSE 메세지 전송 실패: userId={}, content={}, error={}",
					dto.getUserId(), dto.getContent(), e.toString());
				removeEmitter(dto.getUserId(), emitter);
			}
		}

		return atLeastOneSuccess;
	}

	private void removeEmitter(Long userId, SseEmitter emitter) {
		List<SseEmitter> userEmitters = emitters.get(userId);
		if (userEmitters != null) {
			userEmitters.remove(emitter);
			if (userEmitters.isEmpty()) {
				emitters.remove(userId);
			}
		}
	}
}
