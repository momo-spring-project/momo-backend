package com.example.momo.domain.eventhub.application;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.infrastructure.springEvent.NotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//메세지 분기 처리
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventProvider {

	private final MessageFormatUtil messageUtil;
	private final TargetUserProvider targetUserProvider;

	public NotificationEvent processMeeting(MeetingEvents.MeetingEvent meetingEvent) {
		if (meetingEvent instanceof MeetingEvents.Create event) {
			String message = messageUtil.buildCreateMessage(event.categoryName());
			List<Long> userIdList = targetUserProvider.getUserIdList(event.categoryId(), event.latitude(),
				event.longitude());
			return new NotificationEvent(userIdList, event.meetingId(), message);

		}
		if (meetingEvent instanceof MeetingEvents.Update event) {
			String message = messageUtil.buildUpdateMessage(event.meetingName());
			return new NotificationEvent(event.userIdList(), event.meetingId(), message);

		}
		if (meetingEvent instanceof MeetingEvents.Delete event) {
			String message = messageUtil.buildDeleteMessage(event.meetingName());
			return new NotificationEvent(event.userIdList(), event.meetingId(), message);

		}
		if (meetingEvent instanceof MeetingEvents.Join event) {
			String message = messageUtil.buildJoinMessage(event.participantNickname());
			List<Long> userIdList = List.of(event.hostUserId());
			return new NotificationEvent(userIdList, event.meetingId(), message);

		}
		if (meetingEvent instanceof MeetingEvents.Cancel event) {
			String message = messageUtil.buildCancelMessage(event.participantNickname());
			List<Long> userIdList = List.of(event.hostUserId());
			return new NotificationEvent(userIdList, event.meetingId(), message);
		}
		return null;
	}
}
