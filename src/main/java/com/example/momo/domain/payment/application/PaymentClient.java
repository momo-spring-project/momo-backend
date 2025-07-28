package com.example.momo.domain.payment.application;

import java.util.Map;

public interface PaymentClient {

	/**
	 * 결제 승인 처리
	 * @param paymentKey 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 * @return 결제 승인 결과
	 */
	Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount);

	/**
	 * 결제 취소/환불 처리
	 * @param paymentKey 결제 키
	 * @param reason 취소 사유
	 * @return 취소 결과
	 */
	Map<String, Object> cancelPayment(String paymentKey, String reason);

	/**
	 * 결제 정보 조회
	 * @param paymentKey 결제 키
	 * @return 결제 정보
	 */
	Map<String, Object> getPayment(String paymentKey);

	/**
	 * 테스트 Key-in 결제 생성
	 * @param payload 결제 요청 데이터
	 * @param orderId 주문 ID
	 * @return Key-in 결제 생성 결과
	 */
	Map<String, Object> createTestKeyInPayment(Map<String, Object> payload, String orderId);
}
