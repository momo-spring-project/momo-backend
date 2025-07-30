package com.example.momo.global.infrastructure.springEvent.meeting;

public record ParticipationFailedEvents(
	Long meetingId,
	Long userId,
	Long paymentId
) {
}
