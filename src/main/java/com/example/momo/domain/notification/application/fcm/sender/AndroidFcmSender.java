package com.example.momo.domain.notification.application.fcm.sender;

import org.springframework.stereotype.Service;

import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.enums.PlatformType;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class AndroidFcmSender implements FcmSender {
	@Override
	public PlatformType handles() {
		return PlatformType.ANDROID;
	}

	@Override
	public void send(FcmMessageDto dto) throws Exception {
		Message message = Message.builder()
			.setToken(dto.getToken())
			.setNotification(Notification.builder()
				.setTitle(dto.getType().getLabel())
				.setBody(dto.getContent())
				.build())
			.setAndroidConfig(AndroidConfig.builder()
				.setPriority(AndroidConfig.Priority.HIGH)
				.setNotification(AndroidNotification.builder()
					.setSound("default") // 사운드 설정
					.build())
				.build())
			.build();
		FirebaseMessaging.getInstance().send(message);
	}
}
