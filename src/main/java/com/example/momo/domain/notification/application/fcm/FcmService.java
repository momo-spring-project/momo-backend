package com.example.momo.domain.notification.application.fcm;

import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.application.fcm.dto.FcmTokenRequestDto;

/**
 * FCM(Firebase Cloud Messaging)을 활용한 알림 처리 서비스 인터페이스입니다.
 * <p>
 * 유저의 FCM 토큰 저장 및 알림 전송을 처리합니다.
 */
public interface FcmService {

	/**
	 * 전달받은 FCM 토큰을 userId와 함께 저장합니다.
	 *
	 * @param userId     토큰을 저장할 사용자 ID
	 * @param dto        FCM 토큰 요청 DTO
	 */
	void createToken(Long userId, FcmTokenRequestDto dto);

	/**
	 * 해당 유저에게 저장된 FCM 토큰이 존재할 경우, 알림을 각 디바이스에 전송합니다.
	 * <ul>
	 *     <li>모든 디바이스에 전송을 시도하며, 하나라도 성공하면 성공으로 간주합니다.</li>
	 *     <li>전송 실패한 토큰은 삭제되며, 모두 실패한 경우에는 메시지 큐로 재처리를 위한 정보를 저장해야 합니다.</li>
	 *     <li>토큰이 아예 없는 경우도 메시지 큐에 저장이 필요합니다.</li>
	 * </ul>
	 *
	 * @param messageDto 전송할 알림 엔티티 객체
	 */
	void processFcmIfTokenExists(FcmMessageDto messageDto);

}
