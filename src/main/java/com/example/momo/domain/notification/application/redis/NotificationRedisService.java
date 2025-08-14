package com.example.momo.domain.notification.application.redis;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.example.momo.domain.notification.enums.UuidStatus;
import com.example.momo.domain.notification.infra.redis.NotificationRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRedisService {

	private final NotificationRedisRepository redisRepository;
	private final ZoneId zone = ZoneId.of("Asia/Seoul");
	private final DateTimeFormatter basicIsoDate = DateTimeFormatter.BASIC_ISO_DATE;

	public UuidStatus createNotificationUuidOrExist(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			log.info("메세지 허브 리스너 접근 실패 - UUID NULL");
			return UuidStatus.SKIP;
		}

		LocalDate today = LocalDate.now(zone);
		LocalDate yesterday = today.minusDays(1);
		String todayKey = "notification:uuid:" + today.format(basicIsoDate);
		String yesterdayKey = "notification:uuid:" + yesterday.format(basicIsoDate);

		if (redisRepository.isUuidExist(uuid, todayKey, yesterdayKey)) {
			log.info("알림 컨슈머 UUID 중복 - uuid : {}", uuid);
			return UuidStatus.SKIP;
		}

		if (!tryCreateUuid(uuid, todayKey)) {
			return UuidStatus.SAVE_FAIL;
		}

		return UuidStatus.SUCCESS;
	}

	//저장 재시도 후 실패시 로그 생성
	private boolean tryCreateUuid(String uuid, String todayKey) {
		for (int attempt = 1; true; attempt++) {
			try {
				redisRepository.saveUuidKeyWithTodayKey(uuid, todayKey);
				return true;
			} catch (Exception e) {
				if (attempt == 3) {
					log.info("알림 컨슈머 UUID 저장실패 - UUID : {}", uuid);
					return false;
				}
				try {
					Thread.sleep(100L * attempt);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt(); // 상태 복구
					return false;
				}
			}
		}
	}
}
