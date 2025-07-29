package com.example.momo.global.infrastructure.springEvent;

import java.util.List;

/**
 * 모임(Meeting) 도메인에서 발생하는 이벤트를 정의합니다.
 */
public class MeetingEvents {

	/**
	 * 모임 이벤트 마커 인터페이스입니다.
	 */
	public interface MeetingEvent {
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
	public record Create(
		Long meetingId,
		int categoryId,
		String categoryName,
		Double latitude,
		Double longitude
	) implements MeetingEvent {
	}

	/**
	 * 모임 정보가 수정되었을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param meetingName 변경된 모임 이름
	 * @param userIdList 관련 유저 ID 목록
	 */
	public record Update(
		Long meetingId,
		String meetingName,
		List<Long> userIdList
	) implements MeetingEvent {
	}

	/**
	 * 모임이 삭제되었을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param meetingName 삭제된 모임 이름
	 * @param userIdList 관련 유저 ID 목록
	 */
	public record Delete(
		Long meetingId,
		String meetingName,
		List<Long> userIdList
	) implements MeetingEvent {
	}

	public record Register(
		Long meetingId,
		Long userId
	) implements MeetingEvent {
	}

	/**
	 * 사용자가 모임에 참여했을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param hostUserId 주최자 유저 ID
	 * @param participantNickname 참여한 유저 닉네임
	 */
	public record Join(
		Long meetingId,
		Long hostUserId,
		String participantNickname
	) implements MeetingEvent {
	}

	/**
	 * 사용자가 모임 참여를 취소했을 때 발생하는 이벤트입니다.
	 *
	 * @param meetingId 모임 ID
	 * @param hostUserId 주최자 유저 ID
	 * @param participantNickname 취소한 유저 닉네임
	 */
	public record Cancel(
		Long meetingId,
		Long hostUserId,
		String participantNickname
	) implements MeetingEvent {
	}
}
