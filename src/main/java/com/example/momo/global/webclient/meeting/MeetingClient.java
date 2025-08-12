package com.example.momo.global.webclient.meeting;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.webclient.meeting.dto.MeetingClientResponseDto;
import com.example.momo.global.webclient.meeting.dto.ParticipantClientResponseDto;
import com.example.momo.global.webclient.meeting.dto.ParticipantCountClientResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MeetingClient {

	private final WebClient webClient;

	public MeetingClient(@Qualifier("internalWebClient") WebClient webClient) {
		this.webClient = webClient;
	}

	private final static String MEETING_SERVICE_BASE_URI = "/meetings";
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

	/**
	 * 단일 모임 정보 조회
	 *
	 * @param meetingId 조회 모임 ID
	 * @return 모임 정보 DTO
	 */
	public MeetingClientResponseDto getMeeting(Long meetingId) {

		try {
			ApiResponse<MeetingClientResponseDto> response = webClient
				.get()
				.uri(MEETING_SERVICE_BASE_URI + "/{meetingId}", meetingId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<MeetingClientResponseDto>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return null;
			}
			return response.getData();
		} catch (WebClientResponseException e) {

			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return null;
			}

			throw new MeetingClientException("모임 단건 조회 중 오류가 발생했습니다. " + e.getMessage());
		} catch (Exception e) {
			throw new MeetingClientException("모임 단건 조회 중 오류가 발생했습니다. " + e.getMessage());
		}
	}

	public ParticipantClientResponseDto getParticipant(Long meetingId, Long userId) {
		try {
			ApiResponse<ParticipantClientResponseDto> response = webClient
				.get()
				.uri(MEETING_SERVICE_BASE_URI + "/{meetingId}/participants/{userId}", meetingId, userId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<ParticipantClientResponseDto>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return null;
			}

			log.debug("참석자 조회 성공: meetingId={}, participantId={}", meetingId, userId);
			return response.getData();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				log.debug("참석자를 찾을 수 없습니다: meetingId={}, participantId={}", meetingId, userId);
				return null;
			}

			log.error("참석자 조회 실패: meetingId={}, participantId={}, status={}, error={}",
				meetingId, userId, e.getStatusCode(), e.getMessage());
			throw new MeetingClientException("참석자 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	public List<ParticipantClientResponseDto> getParticipants(Long meetingId) {
		// 1. 참가자 ID 목록 조회
		List<Long> participantIds = getParticipantIds(meetingId);

		if (participantIds.isEmpty()) {
			return List.of();
		}

		// 2. 각 참가자의 상세 정보를 개별 조회
		// 주의: 이 방법은 N+1 쿼리 문제가 있으므로, 실제로는 Meeting API에서
		// 참가자 상세 정보를 한 번에 반환하는 API를 추가하는 것이 좋습니다.
		return participantIds.stream()
			.map(participantId -> getParticipant(meetingId, participantId))
			.filter(Objects::nonNull)
			.toList();
	}

	/**
	 * 참가자 ID 목록 조회 - UserService에서 평가 검증용으로 사용
	 * Meeting API에서 ParticipantResponseDto 리스트를 받아서 userId만 추출
	 */
	public List<Long> getParticipantIds(Long meetingId) {
		try {
			log.info("=== MeetingClient.getParticipantIds 시작 ===");
			log.info("요청 meetingId: {}", meetingId);
			log.info("요청 URL: {}", MEETING_SERVICE_BASE_URI + "/" + meetingId + "/participants");

			ApiResponse<List<ParticipantClientResponseDto>> response = webClient
				.get()
				.uri(MEETING_SERVICE_BASE_URI + "/{meetingId}/participants", meetingId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<ParticipantClientResponseDto>>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			log.info("API 응답 전체: {}", response);

			if (response == null) {
				log.error("응답이 null!");
				return List.of();
			}

			if (!response.isSuccess()) {
				log.error("응답 실패: success={}, message={}", response.isSuccess(), response.getMessage());
				return List.of();
			}

			if (response.getData() == null) {
				log.error("응답 데이터가 null!");
				return List.of();
			}

			log.info("응답 데이터 크기: {}", response.getData().size());
			log.info("응답 데이터 내용: {}", response.getData());

			List<Long> participantIds = response.getData().stream()
				.map(participant -> {
					log.info("참가자 정보: id={}, meetingId={}, participantId={}, attendance={}",
						participant.getId(), participant.getMeetingId(),
						participant.getParticipantId(),
						participant.isAttendanceStatus()); // getUserId() -> getParticipantId()
					return participant.getParticipantId(); // getUserId() -> getParticipantId()
				})
				.toList();

			log.info("최종 추출된 participantIds: {}", participantIds);
			log.info("=== MeetingClient.getParticipantIds 완료 ===");
			return participantIds;

		} catch (Exception e) {
			log.error("=== MeetingClient.getParticipantIds 오류 ===", e);
			return List.of();
		}
	}

	public ParticipantCountClientResponseDto getParticipantCount(Long meetingId, Boolean attendance,
		LocalDateTime createdAt) {
		try {
			ApiResponse<ParticipantCountClientResponseDto> response = webClient
				.get()
				.uri(uriBuilder -> uriBuilder
					.path(MEETING_SERVICE_BASE_URI + "/{meetingId}/participants/count")
					.queryParam("attendance", attendance)
					.queryParam("createdAt", createdAt)
					.build(meetingId))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<ParticipantCountClientResponseDto>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return null;
			}

			log.debug("참석자 집계 조회 성공: meetingId={}, attendance={}, createdAt={}", meetingId, attendance, createdAt);
			return response.getData();

		} catch (WebClientResponseException e) {
			log.error("참석자 집계 조회 실패: meetingId={}, attendance={}, createdAt={} status={}, error={}",
				meetingId, attendance, createdAt, e.getStatusCode(), e.getMessage());
			throw new MeetingClientException("참석자 집계 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	public List<MeetingClientResponseDto> getMeetingsByUserId(Long userId) {

		try {
			ApiResponse<List<MeetingClientResponseDto>> response = webClient
				.get()
				.uri(MEETING_SERVICE_BASE_URI + "/user/{userId}", userId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<MeetingClientResponseDto>>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return null;
			}
			return response.getData();
		} catch (WebClientResponseException e) {

			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return null;
			}

			throw new MeetingClientException("모임 목록 조회 중 오류가 발생했습니다. " + e.getMessage());
		} catch (Exception e) {
			throw new MeetingClientException("모임 목록 조회 중 오류가 발생했습니다. " + e.getMessage());
		}
	}
}