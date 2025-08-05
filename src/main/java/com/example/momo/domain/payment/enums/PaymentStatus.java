package com.example.momo.domain.payment.enums;

public enum PaymentStatus {
	PENDING("결제 대기"),      // 결제 시작했지만 아직 완료되지 않음
	COMPLETED("결제 완료"),     // 결제 완료
	FAILED("결제 실패"),        // 결제 실패
	REFUNDED("환불 완료"),      // 환불된 상태
	CANCELED("결제 취소"), //결제 취소(결제 전 포기)
	EXPIRED("만료됨");          // 타임아웃으로 만료

	private final String description;

	PaymentStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}