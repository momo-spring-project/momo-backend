package com.example.momo.global.common.event;

import java.util.List;

public class MeetingEvents {

	public interface MeetingEvent {
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
		String joinedUserNickname
	) implements MeetingEvent {
	}

	public record Cancel(
		Long meetingId,
		Long hostUserId,
		String canceledUserNickname
	) implements MeetingEvent {
	}

}