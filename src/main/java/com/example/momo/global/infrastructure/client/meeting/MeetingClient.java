package com.example.momo.global.infrastructure.client.meeting;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.infrastructure.client.meeting.dto.MeetingClientResponseDto;
import com.example.momo.global.infrastructure.client.meeting.dto.ParticipantClientResponseDto;
import com.example.momo.global.infrastructure.client.meeting.dto.ParticipantCountClientResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingClient {

	private final static String MEETING_SERVICE_BASE_URI = "/api/v2/meetings";
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
	private final WebClient webClient;

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

	public ParticipantClientResponseDto getParticipant(Long meetingId, Long participantId) {
		try {
			ApiResponse<ParticipantClientResponseDto> response = webClient
				.get()
				.uri(MEETING_SERVICE_BASE_URI + "/{meetingId}/participants/{participantId}", meetingId, participantId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<ParticipantClientResponseDto>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return null;
			}

			log.debug("참석자 조회 성공: meetingId={}, participantId={}", meetingId, participantId);
			return response.getData();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				log.debug("참석자를 찾을 수 없습니다: meetingId={}, participantId={}", meetingId, participantId);
				return null;
			}

			log.error("참석자 조회 실패: meetingId={}, participantId={}, status={}, error={}",
				meetingId, participantId, e.getStatusCode(), e.getMessage());
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
	 * 참가자 ID 목록 조회 (기존 API 그대로 사용)
	 * 현재 Meeting API는 List<Long> 참가자 ID만 반환함
	 */
	public List<Long> getParticipantIds(Long meetingId) {
		try {
			ApiResponse<List<Long>> response = webClient
				.get()
				.uri(MEETING_SERVICE_BASE_URI + "/{meetingId}/participants", meetingId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Long>>>() {
				})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return List.of();
			}

			log.debug("참석자 ID 목록 조회 성공: meetingId={}, count={}", meetingId, response.getData().size());
			return response.getData();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				log.debug("참석자 목록을 찾을 수 없습니다: meetingId={}", meetingId);
				return List.of();
			}

			log.error("참석자 목록 조회 실패: meetingId={}, status={}, error={}",
				meetingId, e.getStatusCode(), e.getMessage());
			throw new MeetingClientException("참석자 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	public ParticipantCountClientResponseDto getParticipantCount(Long meetingId, Boolean attendance, LocalDateTime createdAt) {
		try {
			ApiResponse<ParticipantCountClientResponseDto> response = webClient
				.get()
				.uri(uriBuilder -> uriBuilder
					.path(MEETING_SERVICE_BASE_URI + "/{meetingId}/participants/count")
					.queryParam("attendance", attendance)
					.queryParam("createdAt", createdAt)
					.build(meetingId))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<ParticipantCountClientResponseDto>>() {})
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