package com.example.momo.domain.messagehub.application.scheduler;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.application.service.RedisReminderService;
import com.example.momo.domain.messagehub.application.util.MessageFormatUtil;
import com.example.momo.domain.messagehub.enums.AlarmType;
import com.example.momo.domain.messagehub.enums.MessageType;
import com.example.momo.domain.messagehub.event.rabbitmq.producer.NotificationMessageProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderPollingScheduler {
	private final RedisReminderService redisReminderService;
	private final NotificationMessageProducer hubPublisher;
	private final MessageFormatUtil messageFormatUtil;

	@Scheduled(fixedDelay = 10_000)
	public void poll30minBeforeAlarms() {

		List<MeetingReminderMessage> messages =
			redisReminderService.getUpcomingMessages(1000);
		log.debug("[30분전 알림] 조회된 메시지 수: {}", messages.size());

		//발송 성공한 메시지만 모을 리스트
		Set<String> succeededKeys = new HashSet<>();

		for (MeetingReminderMessage message : messages) {
			String content = messageFormatUtil.buildUpcomingMessage(message.getMeetingName());
			log.debug("[30분전 알림] 알림 발행 - userId: {}, content: {}",
				message.getUserId(), content);
			try {
				hubPublisher.publish(
					message.toEvent(content, MessageType.MEETING_UPCOMING.name())
				);
				String uniqueKey = message.getUserId() + ":" + message.getMeetingId();
				succeededKeys.add(uniqueKey);
			} catch (Exception ex) {
				log.error("[30분전 알림] 발송 실패, 재시도 대상 유지 - message={}, error={}",
					message, ex.getMessage(), ex);
			}
		}

		//성공한 메시지만 삭제
		if (!succeededKeys.isEmpty()) {
			redisReminderService.deleteSentMessages(succeededKeys);
			log.debug("[30분전 알림] 발송 완료 후 삭제된 메세지 수 : {}", succeededKeys.size());
		}
	}

	@Scheduled(fixedDelay = 5_000)
	public void pollDayBeforeAlarms() {
		Instant now = Instant.now();
		log.debug("[하루전 알림] 실행 시간: {}", now);
		List<MeetingReminderMessage> messages =
			redisReminderService.getTomorrowMessages(1000);
		log.debug("[하루전 알림] 조회된 메시지 수: {}", messages.size());

		// 발송 성공한 메시지의 uniqueKey만 모음
		Set<String> succeededKeys = new HashSet<>();

		for (MeetingReminderMessage message : messages) {
			String content = messageFormatUtil.buildTomorrowMessage(message.getMeetingName());
			log.debug("[하루전 알림] 알림 발행 - userId: {}, content: {}", message.getUserId(), content);
			try {
				hubPublisher.publish(message.toEvent(content, MessageType.MEETING_TOMORROW.name()));
				String uniqueKey = message.getUserId() + ":" + message.getMeetingId();
				succeededKeys.add(uniqueKey);
			} catch (Exception ex) {
				log.error("[하루전 알림] 발송 실패, 재시도 대상 유지 - message={}, error={}",
					message, ex.getMessage(), ex);
			}

		}
		if (!succeededKeys.isEmpty()) {
			redisReminderService.updateSentMessages(succeededKeys, AlarmType.DAY);
		}

	}
}
