package com.example.momo.domain.messagehub.application.provider;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.util.MessageFormatUtil;
import com.example.momo.domain.messagehub.enums.NotificationEventType;
import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.infrastructure.springEvent.message.FollowEvents;
import com.example.momo.global.infrastructure.springEvent.message.PaymentEvents;
import com.example.momo.global.infrastructure.springEvent.notification.NotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도메인 이벤트를 알림용 {@link NotificationEvent}로 변환하는 프로바이더 클래스입니다.
 * <p>
 * 도메인 이벤트를 받아 알림 메시지를 생성하고,
 * 수신자 ID 및 알림 타입과 함께 {@link NotificationEvent} 객체로 구성합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventProvider {

	private final MessageFormatUtil messageUtil;
	private final TargetUserProvider targetUserProvider;

	public NotificationEvent processMeetingMessage(MeetingEvents.MeetingEvent meetingEvent) {
		if (meetingEvent instanceof MeetingEvents.Create event) {
			String message = messageUtil.buildCreateMessage(event.categoryName());
			List<Long> userIdList = targetUserProvider.getUserIdList(event.categoryId(), event.latitude(),
				event.longitude());
			return new NotificationEvent(userIdList, event.meetingId(),
				NotificationEventType.MEETING_RECOMMENDED.name(),
				message);

		}
		if (meetingEvent instanceof MeetingEvents.Update event) {
			String message = messageUtil.buildUpdateMessage(event.meetingName());
			return new NotificationEvent(event.userIdList(), event.meetingId(),
				NotificationEventType.MEETING_UPDATED.name(),
				message);

		}
		if (meetingEvent instanceof MeetingEvents.Delete event) {
			String message = messageUtil.buildDeleteMessage(event.meetingName());
			return new NotificationEvent(event.userIdList(), event.meetingId(),
				NotificationEventType.MEETING_DELETED.name(),
				message);

		}
		if (meetingEvent instanceof MeetingEvents.Join event) {
			String message = messageUtil.buildJoinMessage(event.participantNickname());
			List<Long> userIdList = List.of(event.hostUserId());
			return new NotificationEvent(userIdList, event.meetingId(), NotificationEventType.MEETING_JOINED.name(),
				message);

		}
		if (meetingEvent instanceof MeetingEvents.Cancel event) {
			String message = messageUtil.buildCancelMessage(event.participantNickname());
			List<Long> userIdList = List.of(event.hostUserId());
			return new NotificationEvent(userIdList, event.meetingId(), NotificationEventType.MEETING_CANCELLED.name(),
				message);
		}
		return null;
	}

	public NotificationEvent processFollowMessage(FollowEvents.FollowEvent followEvent) {
		if (followEvent instanceof FollowEvents.Followed event) {
			String message = messageUtil.buildFollowedMessage(event.followerUserNickname());
			List<Long> userIdList = List.of(event.followedId());
			return new NotificationEvent(userIdList, event.followerId(), NotificationEventType.FOLLOWED.name(),
				message);
		}
		return null;
	}

	public NotificationEvent processPaymentMessage(PaymentEvents.PaymentEvent paymentEvent) {
		if (paymentEvent instanceof PaymentEvents.Paid event) {
			String message = messageUtil.buildPaidMessage();
			List<Long> userIdList = List.of(event.userId());
			return new NotificationEvent(userIdList, event.paymentId(), NotificationEventType.PAID.name(),
				message);
		}
		if (paymentEvent instanceof PaymentEvents.Refunded event) {
			String message = messageUtil.buildPaidMessage();
			List<Long> userIdList = List.of(event.userId());
			return new NotificationEvent(userIdList, event.paymentId(), NotificationEventType.REFUNDED.name(),
				message);
		}
		return null;
	}
}
