package com.example.momo.domain.payment.enums;

/** Outbox 메시지 처리 상태 */
public enum OutboxStatus {
	PENDING,        // 저장만 되고 아직 발행 안 됨
	PUBLISHED,      // MQ 전송 완료
	FAILED,         // 전송 실패 (재시도 대상)
	DEAD_LETTERED   // 재시도 횟수 초과
}