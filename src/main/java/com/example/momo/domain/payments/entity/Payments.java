package com.example.momo.domain.payments.entity;

import com.example.momo.domain.common.entity.BaseCreateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "payments")
@Getter
@Entity
@NoArgsConstructor
public class Payments extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "user_id")
	private Long userId;

	@Column(nullable = false, name = "meeting_id")
	private Long meetingId;

	@Column(nullable = false, name = "amounts")
	private Double amounts;

	// TODO : ENUM 등으로 관리해야하는건지 ?
	@Column(nullable = false, name = "payments_method")
	private String paymentsMethod;

	// TODO : ID인데 String인지 ?
	@Column(nullable = false, name = "pg_transaction_id")
	private Long pgTransactionId;

	// TODO : String인지 ?
	@Column(nullable = false, name = "status")
	private String status;

	// TODO : paidat과 createdAt 차이가 있는지?

	public Payments(Long userId, Long meetingId, Double amounts, String paymentsMethod, Long pgTransactionId,
		String status) {
		this.userId = userId;
		this.meetingId = meetingId;
		this.amounts = amounts;
		this.paymentsMethod = paymentsMethod;
		this.pgTransactionId = pgTransactionId;
		this.status = status;
	}
}
