package com.example.momo.domain.messagehub.application.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.enums.AlarmType;
import com.example.momo.domain.messagehub.infra.redis.RedisReminderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisReminderService {
	private final RedisReminderRepository redisReminderRepository;
	private final ZoneId zone = ZoneId.of("Asia/Seoul");

	public void saveReminderMessage(MeetingReminderMessage message) {
		LocalDateTime meetingDate = message.getMeetingStartTime();
		LocalDateTime now = LocalDateTime.now();

		log.debug("[알림 예약 저장] 저장 시도 - userId: {}, meetingId: {}, meetingStartTime: {}",
			message.getUserId(), message.getMeetingId(), meetingDate);

		// 이미 시작된 모임이라면 저장하지 않음
		if (meetingDate.isBefore(now)) {
			log.info("[알림 예약 저장] 저장 생략 - 과거 모임, userId: {}, meetingId: {}, meetingStartTime: {}",
				message.getUserId(), message.getMeetingId(), meetingDate);
			return;
		}

		// ZSET 에 score = meetingStartTime
		Instant meetingTime = meetingDate.atZone(ZoneId.systemDefault()).toInstant();

		redisReminderRepository.save(message, meetingTime);
		log.debug("[알림 예약 저장] 저장 완료 - userId: {}, meetingId: {}, meetingStartTime: {}",
			message.getUserId(), message.getMeetingId(), meetingDate);
	}

	/** 30분 전 알림 (기존 로직 유지) */
	public List<MeetingReminderMessage> getUpcomingMessages(int max) {
		Instant now = Instant.now();
		log.debug("[30분전 알림] 실행 시간: {}", now);
		Instant nextPoint = now.plus(AlarmType.MIN30.getDuration());
		long nowMs = now.toEpochMilli();
		long toMs = nextPoint.toEpochMilli();

		// ZSet에서 uniqueKey(알림 식별자)만 범위 조회
		Set<String> uniqueKeys = redisReminderRepository.findUniqueKeysByScoreRange(nowMs, toMs, max);

		if (uniqueKeys.isEmpty()) {
			return List.of();
		}

		// Hash에서 uniqueKey로 실제 객체를 한 번에 조회
		List<MeetingReminderMessage> messages = redisReminderRepository.findMessagesByKeys(uniqueKeys);

		return messages;
	}

	/** 하루 전 알림 전용 — 내일 일정만 조회 */
	public List<MeetingReminderMessage> getTomorrowMessages(int max) {
		LocalDate tomorrow = LocalDate.now(zone).plusDays(1);
		Instant startOfTomorrow = tomorrow.atStartOfDay(zone).toInstant();
		Instant endOfTom = tomorrow.atTime(LocalTime.MAX).atZone(zone).toInstant();

		long from = startOfTomorrow.toEpochMilli();
		long to = endOfTom.toEpochMilli();

		log.debug("[하루전 알림] 조회범위 from: {}, to: {}, KST: {} ~ {}", from, to,
			Instant.ofEpochMilli(from).atZone(zone),
			Instant.ofEpochMilli(to).atZone(zone));
		// ZSet에서 uniqueKey(알림 식별자)만 범위 조회
		Set<String> uniqueKeys = redisReminderRepository.findUniqueKeysByScoreRange(from, to, max);

		if (uniqueKeys.isEmpty()) {
			return List.of();
		}

		// Hash에서 uniqueKey로 실제 객체를 한 번에 조회
		List<MeetingReminderMessage> messages = redisReminderRepository.findMessagesByKeys(uniqueKeys);

		// === sentKey 체크 추가! ===

		return messages.stream()
			.filter(Objects::nonNull)
			.filter(msg -> {
				String uniqueKey = msg.getUserId() + ":" + msg.getMeetingId();
				return !redisReminderRepository.isSent(uniqueKey, AlarmType.DAY);
			})
			.collect(Collectors.toList());
	}

	public void updateSentMessages(Collection<String> uniqueKeys, AlarmType alarmType) {
		// 각 uniqueKey에 해당하는 메시지의 "발송 완료" 상태/플래그를 갱신
		redisReminderRepository.markAsSent(uniqueKeys, alarmType);
	}

	public void deleteSentMessages(Set<String> messageSet) {
		redisReminderRepository.deleteSentMessages(messageSet);
	}

}
