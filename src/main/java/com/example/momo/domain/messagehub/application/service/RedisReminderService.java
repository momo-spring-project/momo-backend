package com.example.momo.domain.messagehub.application.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.application.dto.ScoreRangeDto;
import com.example.momo.domain.messagehub.application.util.ReminderKeyUtil;
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

	public void createReminderMessage(MeetingReminderMessage message) {
		LocalDateTime meetingDate = message.getMeetingDate();
		LocalDateTime now = LocalDateTime.now();

		log.debug("[알림 예약 저장] 저장 시도 - userId: {}, meetingId: {}, meetingStartTime: {}",
			message.getUserId(), message.getMeetingId(), meetingDate);

		// 이미 시작된 모임이라면 저장하지 않음
		if (meetingDate == null || meetingDate.isBefore(now)) {
			log.info("[알림 예약 저장] 저장 생략 - 과거 모임, userId: {}, meetingId: {}, meetingStartTime: {}",
				message.getUserId(), message.getMeetingId(), meetingDate);
			return;
		}

		// ZSET 에 score = meetingStartTime
		Instant meetingTime = meetingDate.atZone(ZoneId.systemDefault()).toInstant();

		tryCreateReminderMessage(message, meetingTime.toEpochMilli());
		log.debug("[알림 예약 저장] 저장 완료 - userId: {}, meetingId: {}, meetingStartTime: {}",
			message.getUserId(), message.getMeetingId(), meetingDate);
	}

	private void tryCreateReminderMessage(MeetingReminderMessage message, long meetingTime) {
		String uniqueKey = ReminderKeyUtil.toUniqueKey(message);
		int maxAttempts = 3;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				redisReminderRepository.saveZsetMessage(uniqueKey, meetingTime);
				redisReminderRepository.saveHashMessage(uniqueKey, message);
				break;
			} catch (Exception e) {
				redisReminderRepository.deleteSentMessage(uniqueKey);
				if (attempt == maxAttempts) {
					log.error("[알림 예약 저장 실패] {}회 시도 - userId: {}, meetingId: {}",
						attempt, message.getUserId(), message.getMeetingId());
				} else {
					try {
						Thread.sleep(100); // 100ms 대기 후 재시도
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	/** 30분 전 알림 (기존 로직 유지) */
	public List<MeetingReminderMessage> getUpcomingMessages(int maxCount) {
		Instant now = Instant.now();
		log.debug("[30분전 알림] 실행 시간: {}", now);
		Instant nextPoint = now.plus(AlarmType.MIN30.getDuration());

		ScoreRangeDto rangeDto = ScoreRangeDto.of(now, nextPoint, maxCount);

		// ZSet에서 uniqueKey(알림 식별자)만 범위 조회
		Set<String> uniqueKeys = redisReminderRepository.findUniqueKeysByScoreRange(rangeDto);

		if (uniqueKeys.isEmpty()) {
			return List.of();
		}

		// Hash에서 uniqueKey로 실제 객체를 한 번에 조회
		return redisReminderRepository.findMessagesByKeys(uniqueKeys);
	}

	/** 하루 전 알림 전용 — 내일 일정만 조회 */
	public List<MeetingReminderMessage> getTomorrowMessages(int maxCount) {
		String today = LocalDate.now(zone).format(DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate tomorrow = LocalDate.now(zone).plusDays(1);
		Instant startOfTomorrow = tomorrow.atStartOfDay(zone).toInstant();
		Instant endOfTom = tomorrow.atTime(LocalTime.MAX).atZone(zone).toInstant();

		ScoreRangeDto rangeDto = ScoreRangeDto.of(startOfTomorrow, endOfTom, maxCount);

		Set<String> uniqueKeys = redisReminderRepository.findUniqueKeysByScoreRange(rangeDto);

		if (uniqueKeys.isEmpty()) {
			return List.of();
		}

		// Hash에서 uniqueKey로 실제 객체를 한 번에 조회
		List<MeetingReminderMessage> messages = redisReminderRepository.findMessagesByKeys(uniqueKeys);

		// === sentKey 체크 추가! ===

		String sentKey = ReminderKeyUtil.toSentKeyWithToday(today);

		return messages.stream()
			.filter(Objects::nonNull)
			.filter(msg -> {
				String uniqueKey = ReminderKeyUtil.toUniqueKey(msg);
				String sentMark = ReminderKeyUtil.toSentMark(uniqueKey, AlarmType.DAY);
				return !redisReminderRepository.isSent(sentKey, sentMark);
			})
			.collect(Collectors.toList());
	}

	public void updateSentMessages(Collection<String> uniqueKeys, AlarmType alarmType) {
		String today = LocalDate.now(zone).format(DateTimeFormatter.BASIC_ISO_DATE);
		String sentKey = ReminderKeyUtil.toSentKeyWithToday(today);

		String[] markedMembers = uniqueKeys.stream()
			.map(key -> ReminderKeyUtil.toSentMark(key, alarmType))
			.toArray(String[]::new);

		redisReminderRepository.markAsSent(sentKey, markedMembers);
	}

	//단일 메세지 삭제
	public void deleteSentMessage(MeetingReminderMessage message) {
		String uniqueKey = ReminderKeyUtil.toUniqueKey(message);
		redisReminderRepository.deleteSentMessage(uniqueKey);
	}

	public void deleteSentMessages(Set<String> keys) {

		redisReminderRepository.deleteSentMessages(keys);
	}

	public void deleteOldRemindersByDate(int maxCount) {
		LocalDate today = LocalDate.now();
		Instant startOf = today.minusDays(8).atStartOfDay(zone).toInstant();
		Instant endOf = today.minusDays(2).atTime(LocalTime.MAX).atZone(zone).toInstant();

		ScoreRangeDto rangeDto = ScoreRangeDto.of(startOf, endOf, maxCount);

		Set<String> expiredKeys = redisReminderRepository.findUniqueKeysByScoreRange(rangeDto);

		for (String expiredKey : expiredKeys) {
			redisReminderRepository.deleteSentMessage(expiredKey);
		}
		log.info("[알림 만료] {}건 삭제 완료", expiredKeys.size());

	}

}
