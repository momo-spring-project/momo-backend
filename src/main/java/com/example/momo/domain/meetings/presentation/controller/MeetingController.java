package com.example.momo.domain.meetings.presentation.controller;

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

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.domain.meetings.application.MeetingService;
import com.example.momo.domain.meetings.enums.MeetingStatus;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingPagingResponse;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingResponse;

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
		@RequestBody MeetingStatus status, @AuthenticationPrincipal AuthUser authUser) {

		MeetingResponse response = meetingService.updateMeetingStatus(meetingId, status, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 상태 변경이 성공적으로 완료되었습니다.", response));
	}

	// TODO : 동적 필터 부분은 추후 queryDsl 추가 후 수정, 우선은 제목만 필터링 [status, meetingDate, 위도, 경도 등등]
	@GetMapping
	public ResponseEntity<ApiResponse<MeetingPagingResponse<MeetingResponse>>> searchMeetings(
		@RequestParam(defaultValue = "") String title,
		@RequestParam(defaultValue = "1") @Min(1) int page,
		@RequestParam(defaultValue = "10") @Min(5) int size
	) {

		System.out.println(title);
		MeetingPagingResponse<MeetingResponse> response = meetingService.getMeetings(title, page, size);
		return ResponseEntity.ok(ApiResponse.success("모임 목록 조회가 성공적으로 완료되었습니다.", response));
	}

	@DeleteMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<Void>> deleteMeeting(@PathVariable Long meetingId,
		@AuthenticationPrincipal AuthUser authUser) {

		meetingService.deleteMeeting(meetingId, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("모임 삭제가 성공적으로 완료되었습니다.", null));
	}
}
