package com.example.momo.domain.messagehub.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.momo.global.infrastructure.client.user.UserClient;
import com.example.momo.global.infrastructure.client.user.dto.UserClientResponseDto;

import lombok.RequiredArgsConstructor;

//보내야 하는 유저 정보 조회
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
