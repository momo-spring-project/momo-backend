package com.example.momo.global.infrastructure.client.meeting;

import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.infrastructure.client.meeting.dto.ParticipantClientResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingClient {

	private final WebClient webClient;
	private final static String MEETING_SERVICE_BASE_URI = "/api/v2/meetings";
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

	public ParticipantClientResponseDto getParticipant(Long meetingId, Long participantId) {
		try {
			ApiResponse<ParticipantClientResponseDto> response = webClient
				.get()
				.uri(MEETING_SERVICE_BASE_URI + "/{meetingId}/participants/{participantId}", meetingId, participantId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<ParticipantClientResponseDto>>() {})
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
		try {
			ApiResponse<List<ParticipantClientResponseDto>> response = webClient
				.get()
				.uri(MEETING_SERVICE_BASE_URI + "/{meetingId}", meetingId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<List<ParticipantClientResponseDto>>>() {})
				.timeout(REQUEST_TIMEOUT)
				.block();

			if (response == null || !response.isSuccess()) {
				return null;
			}

			log.debug("참석자 목록 조회 성공: meetingId={}", meetingId);
			return response.getData();

		} catch (WebClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				log.debug("참석자 목록을 찾을 수 없습니다: meetingId={}", meetingId);
				return null;
			}

			log.error("참석자 목록 조회 실패: meetingId={}, status={}, error={}",
				meetingId, e.getStatusCode(), e.getMessage());
			throw new MeetingClientException("참석자 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}