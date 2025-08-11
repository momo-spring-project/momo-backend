package com.example.momo.global.rabbitmq.dto.meeting;

import com.example.momo.global.rabbitmq.constant.RoutingKeys;

public class ParticipantEvents {
	public interface ParticipantEvent {
		Long meetingId();
		Long userId();
	}

	public record Register(
		Long meetingId,
		Long userId
	) implements ParticipantEvent {
	}

	public record Join(
		Long meetingId,
		Long userId,
		Long hostUserId,
		String participantNickname
	) implements ParticipantEvent {
	}

	public record Cancel(
		Long meetingId,
		Long userId,
		Long hostUserId,
		String participantNickname,
		Boolean refundRequired,
		Integer amount
	) implements ParticipantEvent {
	}
}
