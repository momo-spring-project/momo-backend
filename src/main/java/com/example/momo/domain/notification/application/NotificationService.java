package com.example.momo.domain.notification.application;

import java.util.List;

import com.example.momo.domain.notification.application.dto.NotificationMessageDto;
import com.example.momo.domain.notification.application.dto.NotificationRequestDto;
import com.example.momo.domain.notification.application.dto.NotificationResponseDto;

public interface NotificationService {
	/**
	 * 알림 정보를 데이터베이스에 저장합니다.
	 *
	 * @param command 저장할 알림 정보를 담은 객체
	 */
	NotificationResponseDto createNotification(NotificationRequestDto command);

	/**
	 * 알림 정보를 데이터베이스에 저장합니다.
	 *
	 * @param dto 저장할 알림 정보를 담은 객체
	 */
	Long createNotification(NotificationMessageDto dto);

	/**
	 * 특정 사용자의 알림 목록을 조회합니다.
	 *
	 * @param userId 알림을 조회할 사용자의 ID
	 * @return 해당 사용자의 알림 목록
	 */
	List<NotificationResponseDto> getNotifications(Long userId);
}
