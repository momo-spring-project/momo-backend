package com.example.momo.domain.messagehub.application.scheduler;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

	@Scheduled(fixedDelay = 60_000)
	public void poll30minBeforeAlarms() {
		List<MeetingReminderMessage> messages = redisReminderService.getUpcomingMessages(1000);
		log.debug("[30분전 알림] 조회된 메시지 수: {}", messages.size());

		Set<MeetingReminderMessage> succeededMessages = messages.stream()
			.filter(this::publishReminderMessage)
			.collect(Collectors.toSet());

		log.debug("[30분전 알림] 발송 완료 후 삭제된 메세지 수 : {}", succeededMessages.size());

		succeededMessages.forEach(redisReminderService::deleteSentMessage);
	}

	// 알림 발행/성공여부만 책임지는 함수
	private boolean publishReminderMessage(MeetingReminderMessage message) {
		String content = messageFormatUtil.buildUpcomingMessage(message.getMeetingName());
		log.debug("[30분전 알림] 알림 발행 - userId: {}, content: {}", message.getUserId(), content);

		try {
			hubPublisher.publish(message.toEvent(content, MessageType.MEETING_UPCOMING.name()));
			return true;
		} catch (Exception ex) {
			log.error("[30분전 알림] 발송 실패, 재시도 대상 유지 - message={}, error={}",
				message, ex.getMessage(), ex);
			return false;
		}
	}

	@Scheduled(fixedDelay = 1_800_000)
	public void pollDayBeforeAlarms() {
		Instant now = Instant.now();
		log.debug("[하루전 알림] 실행 시간: {}", now);

		List<MeetingReminderMessage> messages = redisReminderService.getTomorrowMessages(1000);
		log.debug("[하루전 알림] 조회된 메시지 수: {}", messages.size());

		// 성공한 메시지의 uniqueKey만 모음
		Set<String> succeededKeys = messages.stream()
			.filter(this::publishTomorrowReminderMessage)
			.map(msg -> msg.getUserId() + ":" + msg.getMeetingId())
			.collect(Collectors.toSet());

		if (!succeededKeys.isEmpty()) {
			redisReminderService.updateSentMessages(succeededKeys, AlarmType.DAY);
		}
	}

	// 발송 성공 여부만 판단
	private boolean publishTomorrowReminderMessage(MeetingReminderMessage message) {
		String content = messageFormatUtil.buildTomorrowMessage(message.getMeetingName());
		log.debug("[하루전 알림] 알림 발행 - userId: {}, content: {}", message.getUserId(), content);
		try {
			hubPublisher.publish(message.toEvent(content, MessageType.MEETING_TOMORROW.name()));
			return true;
		} catch (Exception ex) {
			log.error("[하루전 알림] 발송 실패, 재시도 대상 유지 - message={}, error={}",
				message, ex.getMessage(), ex);
			return false;
		}
	}

	@Scheduled(cron = "0 0 3 * * *")
	public void deleteOldRemindersByZSetScore() {
		redisReminderService.deleteOldRemindersByDate();
	}
}
