package com.example.momo.domain.messagehub.application.provider;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.momo.global.infrastructure.client.user.UserClient;
import com.example.momo.global.infrastructure.client.user.dto.UserClientResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * 알림의 수신 대상이 될 유저 목록을 조회하는 프로바이더 클래스입니다.
 *
 * <p>
 * 카테고리 ID와 위치 정보를 기반으로 {@link UserClient}를 통해
 * 알림을 받아야 할 유저들의 ID 리스트를 반환합니다.
 */
@Component
@RequiredArgsConstructor
public class TargetUserProvider {

	private final UserClient userClient;

	public List<Long> getUserIdList(int categoryId, Double latitude, Double longitude) {

		List<UserClientResponseDto> userClientList = userClient.getUsersByLocationAndCategory(
			List.of(categoryId), latitude, longitude);

		return userClientList.stream()
			.map(UserClientResponseDto::getId)
			.collect(Collectors.toList());
	}
}
