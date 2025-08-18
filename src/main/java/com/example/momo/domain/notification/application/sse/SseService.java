package com.example.momo.domain.notification.application.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.momo.domain.notification.application.sse.dto.SseMessageDto;

/**
 * SSE(Server-Sent Events)를 통한 실시간 알림 전송 서비스 인터페이스입니다.
 * <p>
 * 클라이언트와의 SSE 연결을 관리하고, 연결된 사용자에게 실시간 알림 메시지를 전송합니다.
 */
public interface SseService {

	/**
	 * 사용자 ID를 기반으로 SSE 연결을 생성하고 등록합니다.
	 * <p>
	 * 생성된 {@link SseEmitter}는 일정 시간 동안 유효하며, 타임아웃, 완료, 오류 발생 시 자동 제거됩니다.
	 *
	 * @param userId SSE 연결을 시도하는 사용자 ID
	 * @return 생성된 SseEmitter 인스턴스
	 */
	SseEmitter connect(Long userId);

	/**
	 * 해당 사용자에게 SSE 메시지를 전송합니다.
	 * <p>
	 * 사용자가 연결 상태일 경우 메시지를 전송하고, 실패 시 연결을 제거합니다.
	 *
	 * @param message 전송할 SSE 메시지 DTO
	 * @return 전송 성공 여부 (true: 성공, false: 실패)
	 */
	boolean sendIfConnected(SseMessageDto message);

}
