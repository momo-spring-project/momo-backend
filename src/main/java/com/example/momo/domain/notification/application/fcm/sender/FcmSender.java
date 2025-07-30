package com.example.momo.domain.notification.application.fcm.sender;

import java.util.Set;

import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.domain.notification.enums.PlatformType;

/**
 * 플랫폼(Android, iOS, Web 등)에 따라 FCM 메시지를 전송하는 전략 인터페이스입니다.
 * <p>
 * 구현체는 {@link PlatformType}별로 처리 가능한 플랫폼을 정의하고,
 * 해당 플랫폼의 디바이스에 FCM 메시지를 전송하는 로직을 포함합니다.
 */
public interface FcmSender {

	/**
	 * 이 FcmSender가 처리할 수 있는 플랫폼 타입을 반환합니다.
	 *
	 * @return 지원하는 플랫폼 타입 집합
	 */
	Set<PlatformType> handles();

	/**
	 * 주어진 {@link FcmMessageDto}를 이용하여 FCM 메시지를 전송합니다.
	 * <p>
	 * 이 메서드는 FirebaseMessaging API를 통해 실제 디바이스로 메시지를 전송합니다.
	 *
	 * @param dto 전송할 메시지 정보
	 * @throws Exception 전송 중 오류 발생 시 예외 발생
	 */
	void send(FcmMessageDto dto) throws Exception;
}
