package com.example.momo.global.infrastructure.client.meeting;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.infrastructure.client.meeting.dto.MeetingClientResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingClient {

	private static final String MEETING_SERVICE_BASE_URL = "/api/v1/meetings";
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
				.uri(MEETING_SERVICE_BASE_URL + "/{meetingId}", meetingId)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ApiResponse<MeetingClientResponseDto>>() {
				})
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
}
