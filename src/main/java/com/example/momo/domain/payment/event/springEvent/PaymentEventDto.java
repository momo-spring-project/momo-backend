package com.example.momo.domain.payment.event.springEvent;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEventDto {
	private Long paymentId;
	private Long userId;
	private Long meetingId;
	private Integer amount;
	private String eventType; // PAYMENT_COMPLETED
	private LocalDateTime occurredAt;
	private String failReason;  // 실패 시에만 사용
}
