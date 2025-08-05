package com.example.momo.domain.messagehub.application.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

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
	private final ZoneId zone = ZoneId.systemDefault();

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
	public Set<MeetingReminderMessage> getUpcomingMessages(Instant now, int max, AlarmType type) {
		Instant nextPoint = now.plus(type.getDuration());
		long nowMs = now.toEpochMilli();
		long toMs = nextPoint.toEpochMilli();

		Set<MeetingReminderMessage> candidates =
			redisReminderRepository.findByScoreRange(nowMs, toMs, max);

		if (candidates.isEmpty()) {
			return Set.of();
		}

		log.debug("[30분전 알림] 발송 대기 메시지 수: {}", candidates.size());
		return candidates;
	}

	/** 하루 전 알림 전용 — 내일 일정만 조회 */
	public Set<MeetingReminderMessage> getTomorrowMessages(int max) {
		LocalDate tomorrow = LocalDate.now(zone).plusDays(1);
		Instant startOfTomorrow = tomorrow.atStartOfDay(zone).toInstant();
		Instant endOfTom = tomorrow.atTime(LocalTime.MAX).atZone(zone).toInstant();

		long from = startOfTomorrow.toEpochMilli();
		long to = endOfTom.toEpochMilli();

		Set<MeetingReminderMessage> candidates =
			redisReminderRepository.findByScoreRange(from, to, max);

		if (candidates.isEmpty()) {
			return Set.of();
		}

		log.debug("[하루전 알림] 발송 대기 메시지 수: {}", candidates.size());
		return candidates;
	}

	public void updateSentMessages(Set<MeetingReminderMessage> messageSet, AlarmType type) {
		for (MeetingReminderMessage message : messageSet) {
			redisReminderRepository.markAsSent(message, type);
		}

	}

	public void deleteSentMessages(Set<MeetingReminderMessage> messageSet) {
		redisReminderRepository.deleteSentMessages(messageSet);
	}

}
