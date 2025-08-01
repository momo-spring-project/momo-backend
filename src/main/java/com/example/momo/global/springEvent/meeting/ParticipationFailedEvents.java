package com.example.momo.global.springEvent.meeting;

public record ParticipationFailedEvents(
	Long meetingId,
	Long userId,
	Long paymentId
) {
}
