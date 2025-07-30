package com.example.momo.domain.notification.application.fcm.sender;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.enums.PlatformType;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class IosFcmSender implements FcmSender {
	@Override
	public Set<PlatformType> handles() {
		return Set.of(PlatformType.IOS);
	}

	@Override
	public void send(FcmMessageDto dto) throws Exception {
		Message message = Message.builder()
			.setToken(dto.getToken())
			.setNotification(Notification.builder()
				.setTitle(dto.getType().getLabel())
				.setBody(dto.getContent())
				.build())
			.build();
		FirebaseMessaging.getInstance().send(message);
	}
}
