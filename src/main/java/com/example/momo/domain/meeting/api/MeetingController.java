package com.example.momo.domain.meeting.api;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meeting.domain.dto.response.ParticipantResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.auth.domain.dto.AuthUser;
import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.domain.meeting.application.MeetingService;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.domain.meeting.domain.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meeting.domain.dto.request.MeetingStatusUpdateRequest;
import com.example.momo.domain.meeting.domain.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meeting.domain.dto.response.MeetingPagingResponse;
import com.example.momo.domain.meeting.domain.dto.response.MeetingResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/meetings")
public class MeetingController {

	private final MeetingService meetingService;

	@PostMapping
	public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
		@RequestBody @Valid MeetingCreateRequest request, @AuthenticationPrincipal AuthUser authUser) {

		MeetingResponse response = meetingService.createMeeting(request, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 생성이 성공적으로 완료되었습니다.", response));
	}

	@GetMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<MeetingResponse>> searchMeeting(@PathVariable Long meetingId) {

		MeetingResponse response = meetingService.searchMeeting(meetingId);
		return ResponseEntity.ok(ApiResponse.success("모임 조회가 성공적으로 완료되었습니다.", response));
	}

	@PutMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<MeetingResponse>> updateMeeting(@PathVariable Long meetingId,
		@RequestBody @Valid MeetingUpdateRequest request, @AuthenticationPrincipal AuthUser authUser) {

		MeetingResponse response = meetingService.updateMeeting(request, meetingId, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 수정이 성공적으로 완료되었습니다.", response));
	}

	@PatchMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<MeetingResponse>> updateMeetingStatus(@PathVariable Long meetingId,
		@RequestBody MeetingStatusUpdateRequest request, @AuthenticationPrincipal AuthUser authUser) {

		MeetingResponse response = meetingService.updateMeetingStatus(meetingId, request.getStatus(), authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 상태 변경이 성공적으로 완료되었습니다.", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<MeetingPagingResponse<MeetingResponse>>> searchMeetings(
		@RequestParam(defaultValue = "") String title,
		@RequestParam(required = false) MeetingStatus status,
		@RequestParam(required = false) LocalDateTime meetingDate,
		@RequestParam(defaultValue = "1") @Min(1) int page,
		@RequestParam(defaultValue = "10") @Min(5) int size
	) {

		MeetingPagingResponse<MeetingResponse> response = meetingService.getMeetings(title, status, meetingDate, page,
			size);
		return ResponseEntity.ok(ApiResponse.success("모임 목록 조회가 성공적으로 완료되었습니다.", response));
	}

	@DeleteMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<Void>> deleteMeeting(@PathVariable Long meetingId,
		@AuthenticationPrincipal AuthUser authUser) {

		meetingService.deleteMeeting(meetingId, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 삭제가 성공적으로 완료되었습니다.", null));
	}

	// 모임 참가
	@PostMapping("/{meetingId}/participants")
	public ResponseEntity<ApiResponse<ParticipantResponseDto>> addParticipant(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long meetingId
	) {
		ParticipantResponseDto responseData = meetingService.registerParticipant(authUser.getId(), meetingId);
		ApiResponse<ParticipantResponseDto> response = ApiResponse.success("참가 신청을 완료했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 참가자 조회
	@GetMapping("/{meetingId}/participants")
	public ResponseEntity<ApiResponse<List<Long>>> getParticipants(
		@PathVariable Long meetingId
	) {
		List<Long> responseData = meetingService.getParticipants(meetingId);
		ApiResponse<List<Long>> response = ApiResponse.success("참가자 조회를 성공했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 참가자 취소
	@DeleteMapping("/{meetingId}/participants")
	public ResponseEntity<ApiResponse<ParticipantResponseDto>> cancelParticipant(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long meetingId
	) {
		ParticipantResponseDto responseData = meetingService.cancelParticipant(authUser.getId(), meetingId);
		ApiResponse<ParticipantResponseDto> response = ApiResponse.success("참가 취소를 완료했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// required = false 자동 위치 입력 가능하면 제거
	@PatchMapping("/{meetingId}/participants")
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
