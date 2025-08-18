package com.example.momo.domain.notification.application.fcm.sender;

import org.springframework.stereotype.Service;

import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.enums.PlatformType;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class IosFcmSender implements FcmSender {
	@Override
	public PlatformType handles() {
		return PlatformType.IOS;
	}

	@Override
	public void send(FcmMessageDto dto) throws Exception {
		Message message = Message.builder()
			.setToken(dto.getToken())
			.setNotification(Notification.builder()
				.setTitle(dto.getType().getLabel())
				.setBody(dto.getContent())
				.build())
			.setApnsConfig(ApnsConfig.builder()
				.setAps(Aps.builder()
					.setSound("default") // iOS 사운드 설정
					.setBadge(1)         // 뱃지 1 증가
					.build())
				.build())
			.build();
		FirebaseMessaging.getInstance().send(message);
	}
}
