package com.example.momo.global.infrastructure.springEvent;

import java.util.List;

public class MeetingEvents {

	public sealed interface MeetingEvent permits Create, Update, Delete, Join, Cancel {
		Long meetingId();
	}

	public record Create(
		Long meetingId,
		int categoryId,
		Double latitude,
		Double longitude
	) implements MeetingEvent {
	}

	public record Update(
		Long meetingId,
		List<Long> userIdList
	) implements MeetingEvent {
	}

	public record Delete(
		Long meetingId,
		List<Long> userIdList
	) implements MeetingEvent {
	}

	public record Join(
		Long meetingId,
		Long hostUserId,
		String participantNickname
	) implements MeetingEvent {
	}

	public record Cancel(
		Long meetingId,
		Long hostUserId,
		String participantNickname
	) implements MeetingEvent {
	}

}