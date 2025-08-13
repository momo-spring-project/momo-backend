package com.example.momo.domain.messagehub.application.dto;

import java.time.Instant;

/**
 * Redis ZSET 조회를 위한 점수 범위와 최대 조회 건수 DTO.
 * score 범위는 epoch milli를 double로 변환하여 사용.
 */
public record ScoreRangeDto(double fromScore, double toScore, int maxCount) {
	public static ScoreRangeDto of(Instant from, Instant to, int maxCount) {
		return new ScoreRangeDto((double)from.toEpochMilli(), (double)to.toEpochMilli(), maxCount);
	}
}