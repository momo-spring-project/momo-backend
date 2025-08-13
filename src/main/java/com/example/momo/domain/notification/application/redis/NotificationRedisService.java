package com.example.momo.domain.notification.application.redis;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

	public boolean isUuidExistOrSave(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			log.info("알림 컨슈머 접근 실패 - UUID NULL");
			return false;
		}

		LocalDate today = LocalDate.now(zone);
		LocalDate yesterday = today.minusDays(1);
		String todayKey = "notification:uuid:" + today.format(basicIsoDate);
		String yesterdayKey = "notification:uuid:" + yesterday.format(basicIsoDate);

		if (redisRepository.isUuidYesterdayKeyExist(uuid, yesterdayKey)) {
			return true;
		}

		Long savedUuid = redisRepository.saveUuidKeyWithTodayKey(uuid, todayKey);

		return savedUuid != null && savedUuid == 0;
	}
}
