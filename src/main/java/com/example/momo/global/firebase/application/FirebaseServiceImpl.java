package com.example.momo.global.firebase.application;

import org.springframework.stereotype.Service;

import com.example.momo.global.firebase.application.dto.FirebaseResponseDto;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;

@Service
public class FirebaseServiceImpl implements FirebaseService {
	@Override
	public String send(FirebaseResponseDto dto) {

		//기존에 사용하는 Entity 이름과 겹치므로 주소값 설정
		com.google.firebase.messaging.Notification fcmNotification =
			com.google.firebase.messaging.Notification.builder()
				.setTitle(dto.getType().getLabel())
				.setBody(dto.getContent())
				.build();

		Message message = Message.builder()
			.setToken(dto.getToken())
			.setNotification(fcmNotification)
			.build();

		try {
			return FirebaseMessaging.getInstance().send(message);
		} catch (FirebaseMessagingException e) {
			if (e.getMessagingErrorCode().equals(MessagingErrorCode.INVALID_ARGUMENT)) {
				// 토큰이 유효하지 않은 경우, 오류 코드를 반환
				return e.getMessagingErrorCode().toString();
			} else if (e.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)) {
				// 재발급된 이전 토큰인 경우, 오류 코드를 반환
				return e.getMessagingErrorCode().toString();
			} else { // 그 외, 오류는 런타임 예외로 처리
				throw new RuntimeException(e);
			}
		}
	}
}
