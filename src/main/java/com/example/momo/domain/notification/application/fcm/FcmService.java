package com.example.momo.domain.notification.application.fcm;

import com.example.momo.domain.notification.application.fcm.dto.FcmTokenRequestDto;
import com.example.momo.domain.notification.domain.Notification;

public interface FcmService {
	void processFcmIfTokenExists(Notification notification);

	void createToken(Long userId, FcmTokenRequestDto dto);

}
