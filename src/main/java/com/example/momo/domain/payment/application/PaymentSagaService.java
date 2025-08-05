package com.example.momo.domain.payment.application;

import com.example.momo.domain.payment.domain.Payment;

public interface PaymentSagaService {

	/** 성공 결제 이벤트를 Outbox 테이블에 기록 */
	void handlePaymentSuccess(Payment payment);

	/** 결제 실패 -> 보상 트랜잭션*/
	void compensatePaymentFailure(Long userId, Long meetingId, String reason);

	/** 환불 완료 이벤트를 Outbox 테이블에 기록 */
	void handleRefund(Payment payment);
}
