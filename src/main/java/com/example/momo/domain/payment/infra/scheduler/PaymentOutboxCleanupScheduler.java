package com.example.momo.domain.payment.infra.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.payment.domain.PaymentOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxCleanupScheduler {

	private final PaymentOutboxRepository outboxRepository;

	/**
	 * 매일 새벽 2시에 30일 이상 된 published=true 레코드 삭제
	 */
	@Scheduled(cron = "0 0 2 * * *")
	@Transactional
	public void cleanupOldOutboxRecords() {
		LocalDateTime threshold = LocalDateTime.now().minusDays(30);
		int deleted = outboxRepository.deletePublishedBefore(threshold);
		log.info("Outbox 정리 완료 - 삭제된 레코드: {}", deleted);
	}

	/**
	 * 매주 일요일 새벽 3시에  DLQ 메시지 정리
	 */
	@Scheduled(cron = "0 0 3 * * SUN")
	@Transactional
	public void cleanupOldDlqMessages() {
		LocalDateTime threshold = LocalDateTime.now().minusDays(7);
		int deleted = outboxRepository.deleteDlqMessagesBefore(threshold);
		log.info("DLQ 메시지 정리 완료 - 삭제된 메시지: {}", deleted);
	}
}
