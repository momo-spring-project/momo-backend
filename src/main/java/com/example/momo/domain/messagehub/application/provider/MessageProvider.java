package com.example.momo.domain.messagehub.application.provider;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.application.dto.MessageDto;
import com.example.momo.domain.messagehub.application.service.RedisReminderService;
import com.example.momo.domain.messagehub.application.util.MessageFormatUtil;
import com.example.momo.domain.messagehub.enums.MessageType;
import com.example.momo.global.rabbitmq.dto.follow.FollowAlarmMessages;
import com.example.momo.global.rabbitmq.dto.meeting.MeetingAlarmMessages;
import com.example.momo.global.rabbitmq.dto.payment.PaymentEventMessages;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이벤트 타입별로 알림 메시지를 생성하고,
 * 필요 시 Redis 에 모임 알림(30분 전/하루 전)을 저장·삭제하는 Provider.
 *
 * <p>
 *
 * 모임 관련 이벤트(생성, 수정, 삭제, 참가, 취소) 처리
 * 팔로우 및 결제 이벤트 처리
 * MessageDto 생성 및 메시지 허브 전송 준비
 * RedisReminderService를 통한 알림 예약 데이터 관리
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProvider {

	private final MessageFormatUtil messageUtil;
	private final TargetUserProvider targetUserProvider;
	private final RedisReminderService redisReminderService;
	private final ObjectMapper objectMapper;

	private void saveReminder(Long userId, Long meetingId, String meetingName, LocalDateTime meetingDate) {
		redisReminderService.createReminderMessage(MeetingReminderMessage.builder()
			.userId(userId)
			.meetingId(meetingId)
			.meetingName(meetingName)
			.meetingDate(meetingDate)
			.build());
	}

	private void deleteReminder(Long userId, Long meetingId) {
		redisReminderService.deleteSentMessage(MeetingReminderMessage.builder()
			.userId(userId)
			.meetingId(meetingId)
			.build());
	}

	// 모임 관련 객체 처리
	public MessageDto processMeetingMessage(String type, Object object) {
		if (MEETING_CREATE.equals(type)) {
			MeetingAlarmMessages.Create event = objectMapper.convertValue(object, MeetingAlarmMessages.Create.class);
			saveReminder(event.hostUserId(), event.meetingId(), event.meetingName(), event.meetingDate());
			String message = messageUtil.buildCreateMessage(event.categoryName());
			List<Long> userIdList = targetUserProvider.getUserIdList(event.categoryId(), event.latitude(),
				event.longitude());
			if (userIdList.isEmpty()) {
				log.debug("모임을 추천할만한 인원이 없습니다.");
				return null;

			}
			return new MessageDto(userIdList, event.meetingId(),
				MessageType.MEETING_RECOMMENDED,
				message);

		}
		if (MEETING_UPDATE.equals(type)) {
			MeetingAlarmMessages.Update event = objectMapper.convertValue(object, MeetingAlarmMessages.Update.class);
			for (Long userId : event.userIdList()) {
				saveReminder(userId, event.meetingId(), event.meetingName(), event.meetingDate());
			}
			String message = messageUtil.buildUpdateMessage(event.meetingName());

			return new MessageDto(event.userIdList(), event.meetingId(),
				MessageType.MEETING_UPDATED,
				message);

		}
		if (MEETING_DELETE.equals(type)) {
			MeetingAlarmMessages.Delete event = objectMapper.convertValue(object, MeetingAlarmMessages.Delete.class);
			for (Long userId : event.userIdList()) {
				deleteReminder(userId, event.meetingId());
			}
			String message = messageUtil.buildDeleteMessage(event.meetingName());
			return new MessageDto(event.userIdList(), event.meetingId(),
				MessageType.MEETING_DELETED,
				message);

		}
		if (MEETING_JOIN.equals(type)) {
			MeetingAlarmMessages.Join event = objectMapper.convertValue(object, MeetingAlarmMessages.Join.class);
			saveReminder(event.userId(), event.meetingId(), event.meetingName(), event.meetingDate());
			String message = messageUtil.buildJoinMessage(event.participantNickname());
			List<Long> userIdList = List.of(event.hostUserId());
			return new MessageDto(userIdList, event.meetingId(), MessageType.MEETING_JOINED,
				message);

		}
		if (MEETING_CANCEL.equals(type)) {
			MeetingAlarmMessages.Cancel event = objectMapper.convertValue(object, MeetingAlarmMessages.Cancel.class);
			deleteReminder(event.userId(), event.meetingId());
			String message = messageUtil.buildCancelMessage(event.participantNickname());
			List<Long> userIdList = List.of(event.hostUserId());
			return new MessageDto(userIdList, event.meetingId(),
				MessageType.MEETING_CANCELLED,
				message);
		}
		return null;
	}

	// 팔로우 관련 객체 처리
	public MessageDto processFollowMessage(String type, Object object) {
		if (FOLLOWED.equals(type)) {
			FollowAlarmMessages.Followed event = objectMapper.convertValue(object, FollowAlarmMessages.Followed.class);
			String message = messageUtil.buildFollowedMessage(event.followerUserNickname());
			List<Long> userIdList = List.of(event.followedId());
			return new MessageDto(userIdList, event.followerId(), MessageType.FOLLOWED,
				message);
		}
		return null;
	}

	// 결제 관련 객체 처리
	public MessageDto processPaymentMessage(String type, Object object) {
		if (PAYMENT_COMPLETED.equals(type)) {
			PaymentEventMessages.Completed event = objectMapper.convertValue(object,
				PaymentEventMessages.Completed.class);
			String message = messageUtil.buildPaidMessage();
			List<Long> userIdList = List.of(event.userId());
			return new MessageDto(userIdList, event.paymentId(), MessageType.PAID,
				message);
		}
		if (PAYMENT_FAILED.equals(type)) {
			PaymentEventMessages.Refunded event = objectMapper.convertValue(object,
				PaymentEventMessages.Refunded.class);
			String message = messageUtil.buildRefundedMessage();
			List<Long> userIdList = List.of(event.userId());
			return new MessageDto(userIdList, event.paymentId(), MessageType.REFUNDED,
				message);
		}
		return null;
	}
}
