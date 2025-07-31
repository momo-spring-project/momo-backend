package com.example.momo.domain.notification.application;

import org.springframework.stereotype.Service;

import com.example.momo.domain.notification.application.dto.NotificationRequestDto;
import com.example.momo.domain.notification.application.fcm.FcmService;
import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.application.sse.SseService;
import com.example.momo.domain.notification.application.sse.dto.SseMessageDto;
import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.enums.NotificationType;
import com.example.momo.global.springEvent.notification.MessageEvents;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 알림 저장 및 전송 로직을 담당하는 컴포넌트입니다.
 * <p>
 * 도메인 이벤트 발생 이후, 알림 데이터를 DB에 저장하고 사용자에게 SSE 또는 FCM을 통해 전송합니다.
 * <p>
 * 주요 역할:
 * <ul>
 *     <li>이벤트 타입 → NotificationType 매핑</li>
 *     <li>알림 생성 및 저장</li>
 *     <li>SSE 실시간 전송 시도</li>
 *     <li>SSE 실패 시 FCM 푸시 전송</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHandler {

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
	 * @param event 알림을 유발한 이벤트 정보
	 */
	@Transactional
	public void processNotification(MessageEvents event) {
		NotificationType type;
		if ((type = resolveNotificationType(event.typeName())) == null) {
			return;
		}

		Long targetId = event.targetId();
		String content = event.content();

		for (Long userId : event.userIdList()) {
			//알림 이벤트 DB 저장
			Notification notification = notificationService.createNotification(NotificationRequestDto.builder()
				.userId(userId)
				.targetId(targetId)
				.type(type)
				.content(content)
				.build());

			if (notification == null) {
				log.warn("알림 저장 실패: userId={}, content={}", userId, content);
				//todo : 저장 실패 알림 메세지큐 저장
				continue;
			}

			//SSE 전송 시도 후 결과 반환
			boolean sseSuccess = sseService.sendIfConnected(SseMessageDto.from(notification));

			//SSE 전송 실패 시 FCM 전송 시도
			if (!sseSuccess) {
				fcmService.processFcmIfTokenExists(FcmMessageDto.from(notification));
			}
		}

	}

	//수신한 문자열을 NotificationType으로 변환합니다.
	private NotificationType resolveNotificationType(String typeName) {
		try {
			return NotificationType.valueOf(typeName);
		} catch (IllegalArgumentException | NullPointerException e) {
			log.warn("잘못된 NotificationType 수신: {}", typeName, e);
			return null;
		}
	}
}