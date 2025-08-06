package com.example.momo.global.rabbitmq.dto;

import com.example.momo.global.rabbitmq.constant.RoutingKeys;

public class ParticipantEvents {
	public interface ParticipantEvent {
		Long meetingId();
		Long userId();
		String routingKey();
	}

	public record Register(
		Long meetingId,
		Long userId
	) implements ParticipantEvent {
		@Override
		public String routingKey() {
			return RoutingKeys.PARTICIPANT_REGISTER;
		}
	}

	public record Join(
		Long meetingId,
		Long userId,
		Long hostUserId,
		String participantNickname
	) implements ParticipantEvent {
		@Override
		public String routingKey() {
			return RoutingKeys.PARTICIPANT_JOIN;
		}
	}

	public record CancelRefund(
		Long meetingId,
		Long userId,
		Long hostUserId,
		String participantNickname
	) implements ParticipantEvent {
		@Override
		public String routingKey() {
			return RoutingKeys.PARTICIPANT_CANCEL_REFUND;
		}
	}

	public record CancelNotification(
		Long meetingId,
		Long userId,
		Long hostUserId,
		String participantNickname
	) implements ParticipantEvent {
		@Override
		public String routingKey() {
			return RoutingKeys.PARTICIPANT_CANCEL_NOTIFICATION;
		}
	}
}
