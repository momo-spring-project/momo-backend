package com.example.momo.global.springEvent.meeting;

import java.util.List;

import com.example.momo.global.rabbitMQ.dto.messagehub.EventMessageType;
import com.example.momo.global.rabbitMQ.dto.messagehub.HubEvent;
import com.fasterxml.jackson.annotation.JsonTypeName;

public class MeetingMessageEvents {

	/**
	 * 모임 이벤트 마커 인터페이스입니다.
	 */
	public interface MeetingMessageEvent extends HubEvent {
		Long meetingId();
	}

	/**
	 * 모임이 생성되었을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param categoryId 카테고리 ID
	 * @param categoryName 카테고리 이름
	 * @param latitude 위도
	 * @param longitude 경도
	 */
	@JsonTypeName(EventMessageType.MEETING_CREATE)
	public record Create(
		Long meetingId,
		int categoryId,
		String categoryName,
		Double latitude,
		Double longitude
	) implements MeetingMessageEvent {
	}

	/**
	 * 모임 정보가 수정되었을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param meetingName 변경된 모임 이름
	 * @param userIdList 관련 유저 ID 목록
	 */
	@JsonTypeName(EventMessageType.MEETING_UPDATE)
	public record Update(
		Long meetingId,
		String meetingName,
		List<Long> userIdList
	) implements MeetingMessageEvent, HubEvent {
	}

	/**
	 * 모임이 삭제되었을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param meetingName 삭제된 모임 이름
	 * @param userIdList 관련 유저 ID 목록
	 */
	@JsonTypeName(EventMessageType.MEETING_DELETE)
	public record Delete(
		Long meetingId,
		String meetingName,
		List<Long> userIdList
	) implements MeetingMessageEvent {
	}

	/**
	 * 사용자가 모임에 참여했을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param hostUserId 주최자 유저 ID
	 * @param participantNickname 참여한 유저 닉네임
	 */
	@JsonTypeName(EventMessageType.MEETING_JOIN)
	public record Join(
		Long meetingId,
		Long hostUserId,
		String participantNickname
	) implements MeetingMessageEvent {
	}

	/**
	 * 사용자가 모임 참여를 취소했을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param hostUserId 주최자 유저 ID
	 * @param participantNickname 취소한 유저 닉네임
	 */
	@JsonTypeName(EventMessageType.MEETING_CANCEL)
	public record Cancel(
		Long meetingId,
		Long hostUserId,
		String participantNickname
	) implements MeetingMessageEvent {
	}
}
