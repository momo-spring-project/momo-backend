package com.example.momo.global.sse.application;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.momo.global.socket.dto.NotificationMessage;

//SSE 기본구조. 현재는 사용 X. 추후 사용 고려
public interface SseService {
	SseEmitter connect(Long userId);

	void send(NotificationMessage message);
}
