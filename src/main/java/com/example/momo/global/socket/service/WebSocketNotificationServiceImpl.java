package com.example.momo.global.socket.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.global.socket.dto.WebSocketNotificationDto;
import com.example.momo.global.socket.handler.WebSocketHandler;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

	private final WebSocketHandler webSocketHandler;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void send(WebSocketNotificationDto message) {
		webSocketHandler.sendToUser(message);
	}
}
