package com.example.momo.domain.notification.application;

import org.springframework.stereotype.Component;

import com.example.momo.domain.notification.application.dto.NotificationMessageDto;
import com.example.momo.domain.notification.application.fcm.FcmService;
import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.application.sse.SseService;
import com.example.momo.domain.notification.application.sse.dto.SseMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProvider {
	private final NotificationService notificationService;
	private final SseService sseService;
	private final FcmService fcmService;

	/**
	 * 도메인 이벤트를 기반으로 알림을 저장하고 전송합니다.
	 * <p>
	 * - 이벤트 타입을 파악하여 알림 유형으로 변환하고<br>
	 * - 각 사용자에게 알림을 생성한 후<br>
	 * - 우선적으로 SSE 로 실시간 전송을 시도하며<br>
	 * - SSE 연결이 없을 경우 FCM 푸시로 대체 전송합니다.
	 * <p>
	 * 해당 작업은 하나의 트랜잭션 내에서 수행됩니다.
	 *
	 * @param dto 알림을 유발한 이벤트 정보
	 */
	public Long saveNotificationFromQueue(NotificationMessageDto dto) {
		//알림 이벤트 DB 저장
		Long notificationId = notificationService.createNotification(dto);

		if (notificationId == null) {
			log.warn("알림 저장 실패: userId={}, content={}", dto.getUserId(), dto.getContent());
			return null;
		}

		dto.updateNotificationId(notificationId);
		return notificationId;
	}

	public boolean sendNotificationFromQueue(NotificationMessageDto dto) {

		//SSE 전송 시도 후 결과 반환
		boolean sseSuccess = sseService.sendIfConnected(SseMessageDto.from(dto));

		//SSE 전송 실패 시 FCM 전송 시도
		if (!sseSuccess) {
			return fcmService.processFcmIfTokenExists(FcmMessageDto.from(dto));
		}
		return true;
	}

}
