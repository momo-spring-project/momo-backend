package com.example.momo.global.rabbitmq.dto.meeting;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.global.rabbitmq.constant.EventTypeNames;
import com.example.momo.global.rabbitmq.dto.messagehub.DomainAlarmMessage;
import com.fasterxml.jackson.annotation.JsonTypeName;

public class MeetingAlarmMessages {

	/**
	 * 모임 이벤트 마커 인터페이스입니다.
	 */
	public interface MeetingAlarmMessage extends DomainAlarmMessage {
		Long meetingId();
	}

	/**
	 * 모임이 생성되었을 때 발생하는 이벤트입니다.
	 *
	 * @param hostUserId 모임 호스트 ID
	 * @param meetingId 모임 ID
	 * @param meetingName 모임 이름
	 * @param categoryId 카테고리 ID
	 * @param categoryName 카테고리 이름
	 * @param latitude 위도
	 * @param longitude 경도
	 * @param meetingDate 모임 시작 날짜/시간
	 */
	@JsonTypeName(EventTypeNames.MEETING_CREATE)
	public record Create(
		Long hostUserId,
		Long meetingId,
		String meetingName,
		int categoryId,
		String categoryName,
		Double latitude,
		Double longitude,
		LocalDateTime meetingDate
	) implements MeetingAlarmMessage {
	}

	/**
	 * 모임 정보가 수정되었을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param meetingName 변경된 모임 이름
	 * @param userIdList 관련 유저 ID 목록(Host 유저 ID 포함)
	 * @param meetingDate 모임 시작 날짜/시간
	 */
	@JsonTypeName(EventTypeNames.MEETING_UPDATE)
	public record Update(
		Long meetingId,
		String meetingName,
		List<Long> userIdList,
		LocalDateTime meetingDate
	) implements MeetingAlarmMessage {
	}

	/**
	 * 모임이 삭제되었을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param meetingName 삭제된 모임 이름
	 * @param userIdList 관련 유저 ID 목록(Host 유저 ID 포함)
	 */
	@JsonTypeName(EventTypeNames.MEETING_DELETE)
	public record Delete(
		Long hostUserId,
		Long meetingId,
		String meetingName,
		List<Long> userIdList
	) implements MeetingAlarmMessage {
	}

	/**
	 * 사용자가 모임에 참여했을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param hostUserId 주최자 유저 ID
	 * @param userId 참여한 유저 ID
	 * @param meetingName 모임 이름
	 * @param participantNickname 참여한 유저 닉네임
	 * @param meetingDate 모임 시작 날짜/시간
	 */
	@JsonTypeName(EventTypeNames.MEETING_JOIN)
	public record Join(
		Long meetingId,
		Long hostUserId,
		Long userId,
		String meetingName,
		String participantNickname,
		LocalDateTime meetingDate
	) implements MeetingAlarmMessage {
	}

	/**
	 * 사용자가 모임 참여를 취소했을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param hostUserId 주최자 유저 ID
	 * @param userId 취소한 유저 ID
	 * @param participantNickname 취소한 유저 닉네임
	 */
	@JsonTypeName(EventTypeNames.MEETING_CANCEL)
	public record Cancel(
		Long meetingId,
		Long hostUserId,
		Long userId,
		String participantNickname
	) implements MeetingAlarmMessage {
	}
}
