package com.example.momo.domain.messagehub.application.scheduler;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.application.service.MessageKeyConverter;
import com.example.momo.domain.messagehub.application.service.RedisReminderService;
import com.example.momo.domain.messagehub.application.util.MessageFormatUtil;
import com.example.momo.domain.messagehub.enums.AlarmType;
import com.example.momo.domain.messagehub.enums.MessageType;
import com.example.momo.domain.messagehub.event.rabbitmq.producer.MessageHubProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderPollingScheduler {
	private final RedisReminderService redisReminderService;
	private final MessageHubProducer hubPublisher;
	private final MessageFormatUtil messageFormatUtil;

	@Scheduled(fixedDelay = 60_000)
	public void poll30minBeforeAlarms() {
		List<MeetingReminderMessage> messages = redisReminderService.getUpcomingMessages(1000);
		int messageCount = messages.size();
		log.debug("[30분전 알림] 조회된 메시지 수: {}", messageCount);
		if (messageCount == 0) {
			return;
		}

		Set<String> succeededMessageKeys = messages.stream()
			.filter(this::publishReminderMessage)
			.map(MessageKeyConverter::toUniqueKey)
			.collect(Collectors.toSet());

		redisReminderService.deleteSentMessages(succeededMessageKeys);
		log.debug("[30분전 알림] 발송 완료 후 삭제된 메세지 수 : {}", succeededMessageKeys.size());

	}

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

	@Scheduled(cron = "0 * 10-17 * * *", zone = "Asia/Seoul")
	public void pollDayBeforeAlarms() {
		Instant now = Instant.now();
		log.debug("[하루전 알림] 실행 시간: {}", now);

		List<MeetingReminderMessage> messages = redisReminderService.getTomorrowMessages(250);
		int messageCount = messages.size();
		log.debug("[하루전 알림] 조회된 메시지 수: {}", messageCount);
		if (messageCount == 0) {
			return;
		}

		Set<String> succeededKeys = messages.stream()
			.filter(this::publishTomorrowReminderMessage)
			.map(MessageKeyConverter::toUniqueKey)
			.collect(Collectors.toSet());

		if (!succeededKeys.isEmpty()) {
			redisReminderService.updateSentMessages(succeededKeys, AlarmType.DAY);
		}
	}

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

	@Scheduled(cron = "0 * 02-06 * * *", zone = "Asia/Seoul")
	public void deleteOldRemindersByZSetScore() {
		redisReminderService.deleteOldRemindersByDate(500);
	}
}
