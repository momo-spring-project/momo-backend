package com.example.momo.domain.meetings.presentation.controller;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.domain.meetings.application.MeetingParticipantService;
import com.example.momo.domain.meetings.application.MeetingService;
import com.example.momo.domain.meetings.presentation.dto.ParticipantResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings/{meetingId}/participants")
@RequiredArgsConstructor
public class MeetingParticipantController {

	private final MeetingService meetingService;

	// 모임 참가
	@PostMapping
	public ResponseEntity<ApiResponse<ParticipantResponseDto>> addParticipant(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long meetingId
	) {
		ParticipantResponseDto responseData = meetingService.registerParticipant(authUser.getId(), meetingId);
		ApiResponse<ParticipantResponseDto> response = ApiResponse.success("참가 신청을 완료했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 참가자 조회
	@GetMapping
	public ResponseEntity<ApiResponse<List<Long>>> getParticipants(
		@PathVariable Long meetingId
	) {
		List<Long> responseData = meetingService.getParticipants(meetingId);
		ApiResponse<List<Long>> response = ApiResponse.success("참가자 조회를 성공했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 참가자 취소
	@DeleteMapping
	public ResponseEntity<ApiResponse<ParticipantResponseDto>> cancelParticipant(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long meetingId
	) {
		ParticipantResponseDto responseData = meetingService.cancelParticipant(authUser.getId(), meetingId);
		ApiResponse<ParticipantResponseDto> response = ApiResponse.success("참가 취소를 완료했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// required = false 자동 위치 입력 가능하면 제거
	@PatchMapping
	public ResponseEntity<ApiResponse<ParticipantResponseDto>> updateParticipantStatus(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long meetingId,
		@RequestParam(required = false) Double lat,
		@RequestParam(required = false) Double lng
	) {
		lat = 37.298219;
		lng = 126.966289;
		ParticipantResponseDto responseData =
			meetingService.updateParticipantStatus(authUser.getId(), meetingId, lat, lng);
		ApiResponse<ParticipantResponseDto> response = ApiResponse.success("모임 출석 처리되었습니다", responseData);
		return ResponseEntity.ok(response);
	}
}