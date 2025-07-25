package com.example.momo.global.infrastructure.springEvent.user;

import java.time.LocalDateTime;

/**
 * 회원탈퇴 이벤트
 * 사용자가 탈퇴했을 때 발행되는 이벤트
 */
public record UserWithdrawalEvent(
	Long userId,
	String email,
	String nickname,
	LocalDateTime withdrawalTime
) {
}