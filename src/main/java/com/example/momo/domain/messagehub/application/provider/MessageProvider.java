package com.example.momo.domain.messagehub.application.provider;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MessageDto;
import com.example.momo.domain.messagehub.application.util.MessageFormatUtil;
import com.example.momo.domain.messagehub.enums.MessageType;
import com.example.momo.global.springEvent.follow.FollowMessageEvents;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;
import com.example.momo.global.springEvent.payment.PaymentMessageEvents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도메인 이벤트를 알림용 {@link MessageDto}로 변환하는 프로바이더 클래스입니다.
 * <p>
 * 도메인 이벤트를 받아 알림 메시지를 생성하고,
 * 수신자 ID 및 알림 타입과 함께 {@link MessageDto} 객체로 구성합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProvider {

	private final MessageFormatUtil messageUtil;
	private final TargetUserProvider targetUserProvider;

	public MessageDto processMeetingMessage(MeetingMessageEvents.MeetingMessageEvent meetingEvent) {
		if (meetingEvent instanceof MeetingMessageEvents.Create event) {
			String message = messageUtil.buildCreateMessage(event.categoryName());
			List<Long> userIdList = targetUserProvider.getUserIdList(event.categoryId(), event.latitude(),
				event.longitude());
			if (userIdList.isEmpty()) {
				log.warn("모임을 추천할만한 인원이 없습니다.");
				return null;
			}
			return new MessageDto(userIdList, event.meetingId(),
				MessageType.MEETING_RECOMMENDED,
				message);

		}
		if (meetingEvent instanceof MeetingMessageEvents.Update event) {
			String message = messageUtil.buildUpdateMessage(event.meetingName());
			return new MessageDto(event.userIdList(), event.meetingId(),
				MessageType.MEETING_UPDATED,
				message);

		}
		if (meetingEvent instanceof MeetingMessageEvents.Delete event) {
			String message = messageUtil.buildDeleteMessage(event.meetingName());
			return new MessageDto(event.userIdList(), event.meetingId(),
				MessageType.MEETING_DELETED,
				message);

		}
		if (meetingEvent instanceof MeetingMessageEvents.Join event) {
			String message = messageUtil.buildJoinMessage(event.participantNickname());
			List<Long> userIdList = List.of(event.hostUserId());
			return new MessageDto(userIdList, event.meetingId(), MessageType.MEETING_JOINED,
				message);

		}
		if (meetingEvent instanceof MeetingMessageEvents.Cancel event) {
			String message = messageUtil.buildCancelMessage(event.participantNickname());
			List<Long> userIdList = List.of(event.hostUserId());
			return new MessageDto(userIdList, event.meetingId(),
				MessageType.MEETING_CANCELLED,
				message);
		}
		return null;
	}

	public MessageDto processFollowMessage(FollowMessageEvents.FollowEvent followEvent) {
		if (followEvent instanceof FollowMessageEvents.Followed event) {
			String message = messageUtil.buildFollowedMessage(event.followerUserNickname());
			List<Long> userIdList = List.of(event.followedId());
			return new MessageDto(userIdList, event.followerId(), MessageType.FOLLOWED,
				message);
		}
		return null;
	}

	public MessageDto processPaymentMessage(PaymentMessageEvents.PaymentEvent paymentEvent) {
		if (paymentEvent instanceof PaymentMessageEvents.Paid event) {
			String message = messageUtil.buildPaidMessage();
			List<Long> userIdList = List.of(event.userId());
			return new MessageDto(userIdList, event.paymentId(), MessageType.PAID,
				message);
		}
		if (paymentEvent instanceof PaymentMessageEvents.Refunded event) {
			String message = messageUtil.buildPaidMessage();
			List<Long> userIdList = List.of(event.userId());
			return new MessageDto(userIdList, event.paymentId(), MessageType.REFUNDED,
				message);
		}
		return null;
	}
}
