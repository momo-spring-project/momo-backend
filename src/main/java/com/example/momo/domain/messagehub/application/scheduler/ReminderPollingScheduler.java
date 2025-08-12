package com.example.momo.domain.messagehub.application.scheduler;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.application.service.MessageHubRedisService;
import com.example.momo.domain.messagehub.application.util.MessageFormatUtil;
import com.example.momo.domain.messagehub.application.util.ReminderKeyUtil;
import com.example.momo.domain.messagehub.enums.AlarmType;
import com.example.momo.domain.messagehub.enums.MessageType;
import com.example.momo.domain.messagehub.event.rabbitmq.producer.MessageHubProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 에 저장된 모임 알림(30분 전/하루 전)을 주기적으로 조회·발송·정리하는 스케줄러.
 * 30분 전 알림은 발송 후 즉시 삭제하며,
 * 하루 전 알림은 발송 성공 시 마킹하여 중복 발송을 방지.
 * 또한 오래된 잔여 데이터를 새벽 시간대에 정리하여 Redis 부하를 최소화.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderPollingScheduler {
	private final MessageHubRedisService messageHubRedisService;
	private final MessageHubProducer hubPublisher;
	private final MessageFormatUtil messageFormatUtil;

	/**
	 * 30분 전 알림을 주기적으로 조회하고 발송 후 삭제하는 작업.
	 * 1분 간격으로 실행되며, Redis에서 최대 1000건까지 조회.
	 * 발송 성공 시 해당 알림의 uniqueKey를 ZSET과 HASH에서 삭제하여 중복 방지.
	 */
	@Scheduled(fixedDelay = 60_000)
	public void poll30minBeforeAlarms() {
		List<MeetingReminderMessage> messages = messageHubRedisService.getUpcomingMessages(1000);
		int messageCount = messages.size();
		log.debug("[30분전 알림] 조회된 메시지 수: {}", messageCount);
		if (messageCount == 0) {
			return;
		}

		Set<String> succeededMessageKeys = messages.stream()
			.filter(this::publishReminderMessage)
			.map(ReminderKeyUtil::toUniqueKey)
			.collect(Collectors.toSet());

		messageHubRedisService.deleteSentMessages(succeededMessageKeys);
		log.debug("[30분전 알림] 발송 완료 후 삭제된 메세지 수 : {}", succeededMessageKeys.size());

	}

	//30분 전 알림 발행 및 재시도
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

	/**
	 * 하루 전 알림을 오전 10시부터 오후 5시까지 1분 간격으로 조회·발송하는 작업.
	 * Redis에서 최대 250건까지 조회하며, 발송 성공 시 SET에 마킹하여 중복 발송을 방지.
	 */
	@Scheduled(cron = "0 * 10-17 * * *", zone = "Asia/Seoul")
	public void pollDayBeforeAlarms() {
		Instant now = Instant.now();
		log.debug("[하루전 알림] 실행 시간: {}", now);

		List<MeetingReminderMessage> messages = messageHubRedisService.getTomorrowMessages(250);
		int messageCount = messages.size();
		log.debug("[하루전 알림] 조회된 메시지 수: {}", messageCount);
		if (messageCount == 0) {
			return;
		}

		Set<String> succeededKeys = messages.stream()
			.filter(this::publishTomorrowReminderMessage)
			.map(ReminderKeyUtil::toUniqueKey)
			.collect(Collectors.toSet());

		if (!succeededKeys.isEmpty()) {
			messageHubRedisService.updateSentMessages(succeededKeys, AlarmType.DAY);
		}
	}

	//하루 전 알림 발행 및 재시도
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

	/**
	 * 2~8일 전 범위의 오래된 알림 데이터를 Redis에서 삭제하는 작업.
	 * 새벽 2시부터 6시까지 매시 정각에 실행하여 서비스 부하를 최소화.
	 * 한 번 실행 시 최대 1000건까지 삭제.
	 */
	@Scheduled(cron = "0 0 02-06 * * *", zone = "Asia/Seoul")
	public void deleteOldRemindersByZSetScore() {

		messageHubRedisService.deleteOldRemindersByDate(1000);
	}
}
