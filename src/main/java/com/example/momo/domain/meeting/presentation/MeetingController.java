package com.example.momo.domain.meeting.presentation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.example.momo.domain.auth.application.dto.AuthUser;
import com.example.momo.domain.meeting.application.MeetingService;
import com.example.momo.domain.meeting.application.dto.request.MeetingCreateRequestDto;
import com.example.momo.domain.meeting.application.dto.request.MeetingStatusUpdateRequestDto;
import com.example.momo.domain.meeting.application.dto.request.MeetingUpdateRequestDto;
import com.example.momo.domain.meeting.application.dto.response.MeetingPagingResponseDto;
import com.example.momo.domain.meeting.application.dto.response.MeetingResponseDto;
import com.example.momo.domain.meeting.application.dto.response.ParticipantCountResponseDto;
import com.example.momo.domain.meeting.application.dto.response.ParticipantCreateResponseDto;
import com.example.momo.domain.meeting.application.dto.response.ParticipantResponseDto;
import com.example.momo.domain.meeting.domain.MeetingDocument;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.global.common.dto.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/meetings")
public class MeetingController {

	private final MeetingService meetingService;

	@PostMapping
	public ResponseEntity<ApiResponse<MeetingResponseDto>> createMeeting(
		@RequestBody @Valid MeetingCreateRequestDto request, @AuthenticationPrincipal AuthUser authUser) {

		MeetingResponseDto response = meetingService.createMeeting(request, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 생성이 성공적으로 완료되었습니다.", response));
	}

	@GetMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<MeetingResponseDto>> getMeeting(@PathVariable Long meetingId) {

		MeetingResponseDto response = meetingService.getMeeting(meetingId);
		return ResponseEntity.ok(ApiResponse.success("모임 조회가 성공적으로 완료되었습니다.", response));
	}

	@PutMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<MeetingResponseDto>> updateMeeting(@PathVariable Long meetingId,
		@RequestBody @Valid MeetingUpdateRequestDto request, @AuthenticationPrincipal AuthUser authUser) {

		MeetingResponseDto response = meetingService.updateMeeting(request, meetingId, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 수정이 성공적으로 완료되었습니다.", response));
	}

	@PatchMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<MeetingResponseDto>> updateMeetingStatus(@PathVariable Long meetingId,
		@RequestBody MeetingStatusUpdateRequestDto request, @AuthenticationPrincipal AuthUser authUser) {

		MeetingResponseDto response = meetingService.updateMeetingStatus(meetingId, request.getStatus(),
			authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 상태 변경이 성공적으로 완료되었습니다.", response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<MeetingPagingResponseDto<MeetingDocument>>> getMeetings(
		@RequestParam(defaultValue = "") String title,
		@RequestParam(required = false) MeetingStatus status,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime meetingDate,
		@RequestParam(required = false) Integer categoryId,
		@RequestParam(defaultValue = "1") @Min(1) int page,
		@RequestParam(defaultValue = "10") @Min(5) int size
	) {

		MeetingPagingResponseDto<MeetingDocument> response = meetingService.getMeetings(title, status,
			meetingDate,
			categoryId,
			page,
			size);
		return ResponseEntity.ok(ApiResponse.success("모임 목록 조회가 성공적으로 완료되었습니다.", response));
	}

	@DeleteMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<Void>> deleteMeeting(@PathVariable Long meetingId,
		@AuthenticationPrincipal AuthUser authUser) {

		meetingService.deleteMeeting(meetingId, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 삭제가 성공적으로 완료되었습니다.", null));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<ApiResponse<List<MeetingResponseDto>>> getMeetingsByUserId(@PathVariable Long userId) {

		List<MeetingResponseDto> response = meetingService.getMeetingsByUserId(userId);
		return ResponseEntity.ok(ApiResponse.success("유저 식별자를 통한 모임 목록 조회가 성공적으로 완료되었습니다.", response));
	}

	// 모임 참가
	@PostMapping("/{meetingId}/participants")
	public ResponseEntity<ApiResponse<ParticipantCreateResponseDto>> createParticipant(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long meetingId
	) {
		ParticipantCreateResponseDto responseData = meetingService.createParticipant(authUser.getId(), meetingId);
		ApiResponse<ParticipantCreateResponseDto> response = ApiResponse.success("결제요청을 완료했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 참가자 목록 조회
	@GetMapping("/{meetingId}/participants")
	public ResponseEntity<ApiResponse<List<ParticipantResponseDto>>> getParticipants(
		@PathVariable Long meetingId
	) {
		List<ParticipantResponseDto> responseData = meetingService.getParticipants(meetingId);
		ApiResponse<List<ParticipantResponseDto>> response = ApiResponse.success("참가자 목록 조회를 성공했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 참가자 취소
	@DeleteMapping("/{meetingId}/participants")
	public ResponseEntity<ApiResponse<ParticipantResponseDto>> deleteParticipant(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Long meetingId
	) {
		ParticipantResponseDto responseData = meetingService.deleteParticipant(authUser.getId(), meetingId);
		ApiResponse<ParticipantResponseDto> response = ApiResponse.success("참가취소/환불요청 을 완료했습니다", responseData);
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

	// meetingId 에 0 넣으면 전체 조회 하도록 설정
	@GetMapping("/{meetingId}/participants/count")
	public ResponseEntity<ApiResponse<ParticipantCountResponseDto>> getParticipantCount(
		@PathVariable Long meetingId,
		@RequestParam Long userId,
		@RequestParam(required = false) Boolean attendance,
		@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd-HH-mm-ss") LocalDateTime createdAt
	) {
		ParticipantCountResponseDto responseData = meetingService.getParticipantCount(userId, meetingId, attendance,
			createdAt);
		ApiResponse<ParticipantCountResponseDto> response = ApiResponse.success("참가자 집계가 처리되었습니다", responseData);
		return ResponseEntity.ok(response);
	}
}