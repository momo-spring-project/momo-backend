package com.example.momo.global.infrastructure.springEvent.meeting;

public record ParticipationCancelCompletedEvents(
	Long meetingId,
	Long hostUserId,
	Long userId
) {
}
