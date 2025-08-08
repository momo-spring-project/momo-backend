package com.example.momo.global.rabbitmq.dto.common;

import java.util.UUID;

/**
 * 범용 이벤트 메시지 래퍼.
 * <p>
 * 모든 도메인 이벤트를 공통 포맷으로 감싸서 전달하기 위해 사용합니다.
 * <ul>
 *     <li>{@code eventId} : 이벤트의 고유 식별자(UUID)</li>
 *     <li>{@code type} : 이벤트 타입(도메인 + 액션 조합, 예: {@code "payment.paid"})</li>
 *     <li>{@code data} : 이벤트 페이로드(도메인별 DTO)</li>
 * </ul>
 * <p>
 * {@link #of(String, Object)}로 새로운 이벤트를 생성하거나,
 * {@link #of(String, String, Object)}로 기존 이벤트 ID를 유지한 채 재발행할 수 있습니다.
 *
 * @param uuId   이벤트 고유 ID
 * @param type 이벤트 타입(문자열)
 * @param data 이벤트 데이터(페이로드)
 * @param <T>  페이로드 타입
 */
public record EventWrapper<T>(
	String uuId,
	String type,
	T data
) {
	/**
	 * 새로운 이벤트를 생성합니다.
	 * <p>UUID가 자동 생성되며, 주로 최초 발행 시 사용합니다.</p>
	 *
	 * @param type 이벤트 타입(문자열, 예: {@code "payment.paid"})
	 * @param data 이벤트 데이터(페이로드)
	 * @param <T>  페이로드 타입
	 * @return 생성된 {@link EventWrapper} 인스턴스
	 */
	public static <T> EventWrapper<T> of(String type, T data) {
		return new EventWrapper<>(UUID.randomUUID().toString(), type, data);
	}

	/**
	 * 기존 이벤트 ID를 재사용하여 새로운 이벤트를 생성합니다.
	 * <p>재발행, 허브 전달, 재시도 큐 전송 등에서 동일 이벤트를 추적하기 위해 사용합니다.</p>
	 *
	 * @param uuId   재사용할 이벤트 ID
	 * @param type 이벤트 타입(문자열)
	 * @param data 이벤트 데이터(페이로드)
	 * @param <T>  페이로드 타입
	 * @return 생성된 {@link EventWrapper} 인스턴스
	 */
	public static <T> EventWrapper<T> of(String uuId, String type, T data) {
		return new EventWrapper<>(uuId, type, data);
	}
}