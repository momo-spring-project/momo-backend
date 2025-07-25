package com.example.momo.global.infrastructure.springEvent.message;

/**
 * 결제 도메인에서 발생하는 이벤트를 정의합니다.
 */
public class PaymentEvents {

	/**
	 * 결제 이벤트 마커 인터페이스입니다.
	 */
	public interface PaymentEvent {
		Long userId();

		Long paymentId();
	}

	/**
	 * 결제가 완료되었을 때 발생하는 이벤트입니다.
	 *
	 * @param userId 결제한 유저 ID
	 * @param paymentId 결제 ID
	 */
	public record Paid(
		Long userId,
		Long paymentId
	) implements PaymentEvent {
	}

	/**
	 * 결제가 환불되었을 때 발생하는 이벤트입니다.
	 *
	 * @param userId 환불받은 유저 ID
	 * @param paymentId 결제 ID
	 */
	public record Refunded(
		Long userId,
		Long paymentId
	) implements PaymentEvent {
	}
}