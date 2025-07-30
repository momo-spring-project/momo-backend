package com.example.momo.domain.notification.application.fcm.sender;

import java.util.Set;

import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.enums.PlatformType;

public interface FcmSender {

	Set<PlatformType> handles();

	void send(FcmMessageDto dto) throws Exception;
}
