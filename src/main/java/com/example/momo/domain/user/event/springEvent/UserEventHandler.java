package com.example.momo.domain.user.event.springEvent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.user.application.UserOutboxService;
import com.example.momo.domain.user.event.rabbitmq.producer.UserEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 유저 도메인 스프링 이벤트 핸들러
 * 트랜잭션 커밋 후 RabbitMQ 메시지 발행 및 아웃박스 상태 업데이트 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventHandler {

	private final UserEventProducer userEventProducer;
	private final UserOutboxService userOutboxService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserWithdrawn(UserEvents.Withdrawn event) {
		try {
			log.info("회원탈퇴 이벤트 처리 시작: userId={}", event.userId());

			userEventProducer.publishUserWithdrawn(
				event.userId(),
				event.email(),
				event.nickname()
			);

			userOutboxService.markEventAsPublished(event.userId(), "USER_WITHDRAWN");

			log.info("회원탈퇴 이벤트 처리 완료: userId={}", event.userId());

		} catch (Exception e) {
			log.error("회원탈퇴 이벤트 처리 실패: userId={}, error={}",
				event.userId(), e.getMessage(), e);
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserFollowed(UserEvents.Followed event) {
		try {
			log.info("팔로우 이벤트 처리 시작: followerId={}, followingId={}",
				event.followerId(), event.followingId());

			userEventProducer.publishUserFollowed(
				event.followerId(),
				event.followingId(),
				event.followerNickname()
			);

			userOutboxService.markEventAsPublished(event.followingId(), "USER_FOLLOWED");

			log.info("팔로우 이벤트 처리 완료: followerId={}, followingId={}",
				event.followerId(), event.followingId());

		} catch (Exception e) {
			log.error("팔로우 이벤트 처리 실패: followerId={}, followingId={}, error={}",
				event.followerId(), event.followingId(), e.getMessage(), e);
		}
	}
}