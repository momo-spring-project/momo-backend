package com.example.momo.global.springEvent.follow;

import com.example.momo.global.rabbitMQ.dto.messagehub.DomainMessageEvent;
import com.example.momo.global.rabbitMQ.dto.messagehub.EventMessageType;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * 팔로우 도메인에서 발생하는 메세지 이벤트를 정의합니다.
 */
public class FollowMessageEvents {

	/**
	 * 팔로우 이벤트 마커 인터페이스입니다.
	 */
	public interface FollowEvent extends DomainMessageEvent {
	}

	/**
	 * 사용자가 다른 사용자를 팔로우했을 때 발생하는 이벤트입니다.
	 *
	 * @param followedId 팔로우 당한 유저 ID
	 * @param followerId 팔로우한 유저 ID
	 * @param followerUserNickname 팔로우한 유저 닉네임
	 */
	@JsonTypeName(EventMessageType.FOLLOWED)
	public record Followed(
		Long followedId,
		Long followerId,
		String followerUserNickname
	) implements FollowEvent {
	}
}