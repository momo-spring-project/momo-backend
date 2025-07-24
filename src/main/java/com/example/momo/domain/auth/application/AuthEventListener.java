package com.example.momo.domain.auth.application;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.auth.domain.UserSocial;
import com.example.momo.domain.auth.infra.UserSocialRepository;
import com.example.momo.global.infrastructure.springEvent.user.UserWithdrawalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventListener {
	private final UserSocialRepository userSocialRepository;
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void withdrawUserEvent(UserWithdrawalEvent event){
		try {
			log.info("계정({})에 연동된 소셜 로그인을 모두 삭제합니다.",event.getEmail());
			userSocialRepository.deleteAllByUserId(event.getUserId());
		} catch (Exception e) {
			log.error("계정({})에 연동 소셜 로그인 삭제를 실패했습니다.");
			throw e;
		}
	}
}
