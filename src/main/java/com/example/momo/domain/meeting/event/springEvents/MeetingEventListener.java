package com.example.momo.domain.meeting.event.springEvents;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.meeting.application.MeetingOutboxService;
import com.example.momo.domain.meeting.domain.MeetingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventListener {

	private final MeetingRepository meetingRepository;
	private final MeetingOutboxService meetingOutboxService;

	@Retryable(backoff = @Backoff(delay = 1000, multiplier = 2))
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void saveMeetingEventListener(MeetingElasticEvents.Save event) {

		try {
			meetingRepository.saveMeetingElastic(event.meeting());
			meetingOutboxService.markEventAsPublished(event.outboxId());
		} catch (Exception e) {
			log.error("elasticsearch 저장 중 예외가 발생함");
			throw e;
		}

	}

	@Retryable(backoff = @Backoff(delay = 1000, multiplier = 2))
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void deleteMeetingEventListener(MeetingElasticEvents.Delete event) {

		try {
			meetingRepository.deleteMeetingElastic(event.meeting());
			meetingOutboxService.markEventAsPublished(event.outboxId());
		} catch (Exception e) {
			log.error("elasticsearch 삭제 중 예외가 발생함");
			throw e;
		}
	}
}
