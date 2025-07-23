package com.example.momo.global.infrastructure.client.user;

import java.time.Duration;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.momo.global.infrastructure.client.user.dto.UserClientResponseDto;
import com.example.momo.global.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 유저 도메인과의 통신을 담당하는 웹클라이언트
 * 다른 도메인에서 유저 정보가 필요할 때 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserClient {

	private final WebClient webClient;
	private static final String USER_SERVICE_BASE_URL = "/api/v1/users";
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

	/**
	 * 단일 사용자 정보 조회
	 *
	 * @param userId 조회할 사용자 ID
	 * @return 사용자 정보 DTO (Optional로 감싸서 반환)
	 * @throws UserClientException 통신 오류 시 (404는 예외가 아님)
	 */
	public UserClientResponseDto getUser(Long userId) {
		try {
			ApiResponse<UserClientResponseDto> response = webClient
				.get()
				.uri(USER_SERVICE_BASE_URL + "/{userId}", userId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<UserClientResponseDto>>() {})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return null; // 호출하는 쪽에서 null 체크로 판단
			}

			log.debug("사용자 정보 조회 성공: userId={}", userId);
			return response.getData();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				log.debug("사용자를 찾을 수 없습니다: userId={}", userId);
				return null; // 404는 정상적인 응답으로 처리
			}
			// 실제 통신 오류만 예외로 처리
			log.error("사용자 정보 조회 실패: userId={}, status={}, error={}",
				userId, e.getStatusCode(), e.getMessage());
			throw new UserClientException("사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
		} catch (Exception e) {
			log.error("사용자 정보 조회 실패: userId={}, error={}", userId, e.getMessage());
			throw new UserClientException("사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	/**
	 * 다중 사용자 정보 조회
	 *
	 * @param userIds 조회할 사용자 ID 목록
	 * @return 사용자 정보 DTO 목록
	 * @throws UserClientException 통신 오류 시
	 */
	public List<UserClientResponseDto> getUsers(List<Long> userIds) {
		try {
			if (userIds == null || userIds.isEmpty()) {
				return List.of();
			}

			// userIds를 쿼리 파라미터로 변환
			String userIdsParam = String.join(",", userIds.stream().map(String::valueOf).toList());

			ApiResponse<List<UserClientResponseDto>> response = webClient
				.get()
				.uri(uriBuilder -> uriBuilder
					.path(USER_SERVICE_BASE_URL + "/batch")
					.queryParam("userIds", userIdsParam)
					.build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<UserClientResponseDto>>>() {})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				throw new UserClientException("사용자 목록 정보를 가져오는데 실패했습니다.");
			}

			log.debug("사용자 목록 조회 성공: userIds={}, count={}", userIds, response.getData().size());
			return response.getData();

		} catch (WebClientResponseException e) {
			log.error("사용자 목록 조회 실패: userIds={}, status={}, error={}",
				userIds, e.getStatusCode(), e.getMessage());
			throw new UserClientException("사용자 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		} catch (Exception e) {
			log.error("사용자 목록 조회 실패: userIds={}, error={}", userIds, e.getMessage());
			throw new UserClientException("사용자 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	/**
	 * 사용자 존재 여부 확인
	 *
	 * @param userId 확인할 사용자 ID
	 * @return 사용자 존재 여부
	 */
	public boolean existsUser(Long userId) {
		UserClientResponseDto user = getUser(userId);
		return user != null;
	}

	/**
	 * 다중 사용자 존재 여부 확인
	 *
	 * @param userIds 확인할 사용자 ID 목록
	 * @return 존재하는 사용자 ID 목록
	 */
	public List<Long> getExistingUserIds(List<Long> userIds) {
		try {
			List<UserClientResponseDto> users = getUsers(userIds);
			return users.stream()
				.map(UserClientResponseDto::getId)
				.toList();
		} catch (UserClientException e) {
			log.warn("사용자 존재 여부 확인 실패: userIds={}", userIds);
			return List.of();
		}
	}

	/**
	 * 사용자 닉네임만 조회 (가벼운 조회용)
	 *
	 * @param userId 조회할 사용자 ID
	 * @return 사용자 닉네임
	 */
	public String getUserNickname(Long userId) {
		UserClientResponseDto user = getUser(userId);
		return user.getNickname();
	}
}