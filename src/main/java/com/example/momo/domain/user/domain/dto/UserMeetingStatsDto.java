package com.example.momo.domain.user.domain.dto;

/**
 * 사용자의 모임 참가 통계 정보를 담는 DTO
 *
 * @param totalParticipation 총 참가 신청한 모임 수
 * @param attendedMeetings 실제 참석한 모임 수
 * @param recentParticipation 최근 3개월 참가한 모임 수
 */
public record UserMeetingStatsDto(
	long totalParticipation,
	long attendedMeetings,
	long recentParticipation
) {
	/**
	 * 참석률 계산
	 * @return 참석률 (0.0 ~ 1.0)
	 */
	public double getAttendanceRate() {
		if (totalParticipation == 0) {
			return 0.0;
		}
		return (double)attendedMeetings / totalParticipation;
	}

	/**
	 * 월 평균 참가 횟수 계산 (최근 3개월 기준)
	 * @return 월 평균 참가 횟수
	 */
	public double getMonthlyAverage() {
		return recentParticipation / 3.0;
	}
}