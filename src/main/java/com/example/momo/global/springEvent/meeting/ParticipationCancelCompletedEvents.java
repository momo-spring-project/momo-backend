package com.example.momo.global.springEvent.meeting;

public record ParticipationCancelCompletedEvents(
	Long meetingId,
	Long hostUserId,
	Long userId
) {
}
