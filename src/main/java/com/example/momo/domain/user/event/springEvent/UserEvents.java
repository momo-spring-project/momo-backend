package com.example.momo.domain.user.event.springEvent;

import java.time.LocalDateTime;

/**
 * 유저 도메인 스프링 이벤트 통합 관리 클래스
 * 현재는 회원탈퇴 이벤트만 구현 (필요시 확장 예정)
 */
public class UserEvents {

	/**
	 * 회원탈퇴 이벤트
	 */
	public record Withdrawn(
		Long userId,
		String email,
		String nickname,
		LocalDateTime withdrawnAt
	) {
		public Withdrawn(Long userId, String email, String nickname) {
			this(userId, email, nickname, LocalDateTime.now());
		}
	}
}