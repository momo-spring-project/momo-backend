package com.example.momo.global.webclient.user;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.momo.domain.user.application.dto.UserAuthResponseDto;
import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 유저 도메인과의 통신을 담당하는 웹클라이언트
 * 다른 도메인에서 유저 정보가 필요할 때 사용
 */
@Slf4j
@Component
public class UserClient {

	private final WebClient webClient;

	public UserClient(@Qualifier("internalWebClient") WebClient webClient) {
		this.webClient = webClient;
	}

	private static final String USER_SERVICE_BASE_URL = "/users";
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

	/**
	 * 단일 사용자 정보 조회
	 */
	public UserClientResponseDto getUser(Long userId) {
		try {
			ApiResponse<UserClientResponseDto> response = webClient
				.get()
				.uri(USER_SERVICE_BASE_URL + "/{userId}", userId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<UserClientResponseDto>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			return (response != null && response.isSuccess()) ? response.getData() : null;
		} catch (Exception e) {
			log.error("사용자 조회 실패: userId={}, error={}", userId, e.getMessage());
			return null;
		}
	}

	/**
	 * 다중 사용자 정보 조회
	 */
	public List<UserClientResponseDto> getUsers(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return List.of();
		}

		try {
			ApiResponse<List<UserClientResponseDto>> response = webClient
				.get()
				.uri(uriBuilder -> uriBuilder
					.path(USER_SERVICE_BASE_URL)
					.queryParam("ids", userIds.toArray())
					.build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<UserClientResponseDto>>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			return (response != null && response.isSuccess()) ? response.getData() : List.of();
		} catch (Exception e) {
			log.error("사용자 목록 조회 실패: userIds={}, error={}", userIds, e.getMessage());
			return List.of();
		}
	}

	/**
	 * 사용자 존재 여부 확인
	 */
	public List<Long> getExistingUserIds(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return List.of();
		}

		try {
			ApiResponse<List<Long>> response = webClient
				.get()
				.uri(uriBuilder -> uriBuilder
					.path(USER_SERVICE_BASE_URL + "/exists")
					.queryParam("ids", userIds.toArray())
					.build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Long>>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			return (response != null && response.isSuccess()) ? response.getData() : List.of();
		} catch (Exception e) {
			log.error("사용자 존재 여부 확인 실패: userIds={}, error={}", userIds, e.getMessage());
			return List.of();
		}
	}

	/**
	 * Auth 도메인 전용 - 이메일로 사용자 정보 조회 (비밀번호 포함)
	 */
	public UserAuthResponseDto getUserByEmailForAuth(String email) {
		if (email == null || email.trim().isEmpty()) {
			log.warn("이메일이 비어있습니다.");
			return null;
		}

		try {
			ApiResponse<UserAuthResponseDto> response = webClient
				.get()
				.uri(uriBuilder -> uriBuilder
					.path(USER_SERVICE_BASE_URL + "/internal/by-email")
					.queryParam("email", email)
					.build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<UserAuthResponseDto>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response != null && response.isSuccess() && response.getData() != null) {
				log.debug("Auth용 이메일로 사용자 조회 성공: email={}", email);
				return response.getData(); // 그대로 반환
			} else {
				log.debug("해당 이메일의 사용자가 존재하지 않습니다: email={}", email);
				return null;
			}
		} catch (Exception e) {
			log.error("Auth용 이메일로 사용자 조회 실패: email={}, error={}", email, e.getMessage());
			return null;
		}
	}

	/**
	 * 카테고리와 위치 조건으로 사용자 검색
	 */
	public List<UserClientResponseDto> getUsersByLocationAndCategory(
		List<Integer> categoryIds,
		Double latitude,
		Double longitude
	) {
		try {
			UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromPath(USER_SERVICE_BASE_URL + "/filter");

			if (categoryIds != null && !categoryIds.isEmpty()) {
				uriBuilder.queryParam("categoryIds", categoryIds.toArray());
			}
			if (latitude != null) {
				uriBuilder.queryParam("latitude", latitude);
			}
			if (longitude != null) {
				uriBuilder.queryParam("longitude", longitude);
			}

			ApiResponse<List<UserClientResponseDto>> response = webClient
				.get()
				.uri(uriBuilder.build().toUri())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<UserClientResponseDto>>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			return (response != null && response.isSuccess()) ? response.getData() : List.of();
		} catch (Exception e) {
			log.error("사용자 검색 실패: categoryIds={}, latitude={}, longitude={}, error={}",
				categoryIds, latitude, longitude, e.getMessage());
			return List.of();
		}
	}
}