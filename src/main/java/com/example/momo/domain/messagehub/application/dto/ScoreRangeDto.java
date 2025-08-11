package com.example.momo.domain.messagehub.application.dto;

import java.time.Instant;

public record ScoreRangeDto(double fromScore, double toScore, int maxCount) {
	public static ScoreRangeDto of(Instant from, Instant to, int maxCount) {
		return new ScoreRangeDto((double)from.toEpochMilli(), (double)to.toEpochMilli(), maxCount);
	}
}