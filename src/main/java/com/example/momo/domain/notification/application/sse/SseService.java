package com.example.momo.domain.notification.application.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.momo.domain.notification.application.sse.dto.SseMessageDto;

public interface SseService {
	SseEmitter connect(Long userId);

	boolean sendIfConnected(SseMessageDto message);

}
