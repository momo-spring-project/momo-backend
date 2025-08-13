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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.application.dto.ScoreRangeDto;
import com.example.momo.domain.messagehub.application.util.ReminderKeyUtil;
import com.example.momo.domain.messagehub.enums.AlarmType;
import com.example.momo.domain.messagehub.infra.redis.MessageHubRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis를 이용해 모임 알림 예약·조회·삭제·마킹을 처리하는 서비스.
 * 30분 전/하루 전 알림의 저장, 발송 대상 조회, 발송 후 중복 방지 마킹, 오래된 데이터 정리 기능을 제공.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHubRedisService {
	private final MessageHubRedisRepository messageHubRedisRepository;
	private final ZoneId zone = ZoneId.of("Asia/Seoul");
	private final DateTimeFormatter basicIsoDate = DateTimeFormatter.BASIC_ISO_DATE;

	/**
	 * 모임 알림 예약 데이터를 Redis에 저장.
	 * 이미 시작된 모임은 저장하지 않으며,
	 * ZSET의 score를 모임 시작 시간(epoch milli)으로 설정.
	 */
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

	//저장 재시도 후 실패시 로그 생성
	private void tryCreateReminderMessage(MeetingReminderMessage message, long meetingTime) {
		String uniqueKey = ReminderKeyUtil.toUniqueKey(message);
		int maxAttempts = 3;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				messageHubRedisRepository.saveMessage(uniqueKey, meetingTime, message);
				break;
			} catch (Exception e) {
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

	/**
	 * 30분 전 알림 발송 대상 조회.
	 * 현재 시각부터 30분 이내의 score 범위를 ZSET에서 조회해 uniqueKey 목록을 얻고,
	 * 해당 키로 HASH에서 실제 알림 데이터를 한 번에 조회하여 반환.
	 * @param maxCount : 한번에 가지고 오는 최대 조회 수
	 */
	public List<MeetingReminderMessage> getUpcomingMessages(int maxCount) {
		Instant now = Instant.now();
		log.debug("[30분전 알림] 실행 시간: {}", now);
		Instant nextPoint = now.plus(AlarmType.MIN30.getDuration());

		ScoreRangeDto rangeDto = ScoreRangeDto.of(now, nextPoint, maxCount);

		// ZSet에서 uniqueKey(알림 식별자)만 범위 조회
		Set<String> uniqueKeys = messageHubRedisRepository.findUniqueKeysByScoreRange(rangeDto);

		if (uniqueKeys.isEmpty()) {
			return List.of();
		}

		// Hash에서 uniqueKey로 실제 객체를 한 번에 조회
		return messageHubRedisRepository.findMessagesByKeys(uniqueKeys);
	}

	/**
	 * 하루 전 알림 발송 대상 조회.
	 * 내일 0시부터 23:59:59까지의 score 범위를 ZSET에서 조회해 uniqueKey 목록을 얻고,
	 * 해당 키로 HASH에서 실제 알림 데이터를 한 번에 조회.
	 * 이미 발송 마킹(SET)에 등록된 알림은 제외하여 중복 발송을 방지.
	 * @param maxCount : 한번에 가지고 오는 최대 조회 수
	 */
	public List<MeetingReminderMessage> getTomorrowMessages(int maxCount) {
		//비교 대상 날짜 타입 생성
		String today = LocalDate.now(zone).format(basicIsoDate);
		LocalDate tomorrow = LocalDate.now(zone).plusDays(1);
		Instant startOfTomorrow = tomorrow.atStartOfDay(zone).toInstant();
		Instant endOfTom = tomorrow.atTime(LocalTime.MAX).atZone(zone).toInstant();

		ScoreRangeDto rangeDto = ScoreRangeDto.of(startOfTomorrow, endOfTom, maxCount);

		//키만 조회
		Set<String> uniqueKeys = messageHubRedisRepository.findUniqueKeysByScoreRange(rangeDto);

		if (uniqueKeys.isEmpty()) {
			return List.of();
		}

		//객체 조회
		List<MeetingReminderMessage> messages = messageHubRedisRepository.findMessagesByKeys(uniqueKeys);

		String sentKey = ReminderKeyUtil.toSentKeyWithToday(today);

		//Mark Key(이미 전송된 Key) 필터링 후 반환
		return messages.stream()
			.filter(Objects::nonNull)
			.filter(msg -> {
				String uniqueKey = ReminderKeyUtil.toUniqueKey(msg);
				String sentMark = ReminderKeyUtil.toSentMark(uniqueKey, AlarmType.DAY);
				return !messageHubRedisRepository.isSent(sentKey, sentMark);
			})
			.collect(Collectors.toList());
	}

	/**
	 * 발송된 알림을 SET에 마킹하여 중복 발송을 방지.
	 * uniqueKey와 알림 타입을 조합해 멤버를 생성하고,
	 * 오늘 날짜 기반의 sentKey에 저장 후 TTL(만료 시간)을 적용.
	 */
	public void updateSentMessages(Collection<String> uniqueKeys, AlarmType alarmType) {
		String today = LocalDate.now(zone).format(basicIsoDate);
		String sentKey = ReminderKeyUtil.toSentKeyWithToday(today);

		String[] markedMembers = uniqueKeys.stream()
			.map(key -> ReminderKeyUtil.toSentMark(key, alarmType))
			.toArray(String[]::new);

		messageHubRedisRepository.markAsSent(sentKey, markedMembers);
	}

	//단건 삭제
	public void deleteSentMessage(MeetingReminderMessage message) {
		String uniqueKey = ReminderKeyUtil.toUniqueKey(message);
		messageHubRedisRepository.deleteSentMessage(uniqueKey);
	}

	//다건 삭제
	public void deleteSentMessages(Set<String> keys) {

		messageHubRedisRepository.deleteSentMessages(keys);
	}

	/**
	 * 오래된 알림(2~8일 전)을 점수 범위로 조회해 일괄 삭제.
	 * ZSET에서 uniqueKey를 조회한 뒤 ZSET/HASH 동시 삭제로 정리.
	 * 한 번에 최대 maxCount건까지 처리하여 부하 제한.
	 * 삭제 완료 후 삭제 건수를 로그로 기록.
	 */
	public void deleteOldRemindersByDate(int maxCount) {
		LocalDate today = LocalDate.now(zone);
		Instant startOf = today.minusDays(8).atStartOfDay(zone).toInstant();
		Instant endOf = today.minusDays(2).atTime(LocalTime.MAX).atZone(zone).toInstant();

		ScoreRangeDto rangeDto = ScoreRangeDto.of(startOf, endOf, maxCount);

		Set<String> expiredKeys = messageHubRedisRepository.findUniqueKeysByScoreRange(rangeDto);

		messageHubRedisRepository.deleteSentMessages(expiredKeys);

		log.info("[알림 만료] {}건 삭제 완료", expiredKeys.size());

	}

	public boolean isUuidExistOrSave(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			log.info("메세지 허브 리스너 접근 실패 - UUID NULL");
			return false;
		}

		LocalDate today = LocalDate.now(zone);
		String todayKey = ReminderKeyUtil.toUuidMarkKey(today.format(basicIsoDate));
		String yesterdayKey = ReminderKeyUtil.toUuidMarkKey(today.minusDays(1).format(basicIsoDate));

		if (messageHubRedisRepository.isUuidYesterdayKeyExist(uuid, yesterdayKey)) {
			return true;
		}

		Long savedUuid = messageHubRedisRepository.saveUuidKeyWithTodayKey(uuid, todayKey);

		return savedUuid != null && savedUuid == 0;
	}
}
