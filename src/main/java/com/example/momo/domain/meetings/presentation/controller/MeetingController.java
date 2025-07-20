package com.example.momo.domain.meetings.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.domain.meetings.application.MeetingService;
import com.example.momo.domain.meetings.enums.MeetingStatus;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingResponse;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/meetings")
public class MeetingController {

	private final MeetingService meetingService;

	@PostMapping
	public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
		@RequestBody @Valid MeetingCreateRequest request) {

		// TODO : 인증 개발 후 수정
		Long userId = 1L;
		MeetingResponse response = meetingService.createMeeting(request, userId);
		return ResponseEntity.ok(ApiResponse.success("모임 생성이 성공적으로 완료되었습니다.", response));
	}

	@GetMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<MeetingResponse>> searchMeeting(@PathVariable Long meetingId) {

		MeetingResponse response = meetingService.searchMeeting(meetingId);
		return ResponseEntity.ok(ApiResponse.success("모임 조회가 성공적으로 완료되었습니다.", response));
	}

	@PutMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<MeetingResponse>> updateMeeting(@PathVariable Long meetingId,
		@RequestBody @Valid MeetingUpdateRequest request) {

		// TODO : 인증 개발 후 수정
		Long userId = 1L;
		MeetingResponse response = meetingService.updateMeeting(request, meetingId, userId);
		return ResponseEntity.ok(ApiResponse.success("모임 수정이 성공적으로 완료되었습니다.", response));
	}

	@PatchMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<MeetingResponse>> updateMeetingStatus(@PathVariable Long meetingId,
		@RequestBody MeetingStatus status) {

		// TODO : 인증 개발 후 수정
		Long userId = 1L;
		MeetingResponse response = meetingService.updateMeetingStatus(meetingId, status);
		return ResponseEntity.ok(ApiResponse.success("모임 상태 변경이 성공적으로 완료되었습니다.", response));
	}

	// TODO : 응답 형식 만들기
	@GetMapping
	public ResponseEntity<ApiResponse<Void>> searchMeetings(
		@RequestParam Long categoryId,
		@RequestParam int page,
		@RequestParam int size,
		@RequestParam double latitude,
		@RequestParam double longitude) {

		return ResponseEntity.ok(ApiResponse.success("모임 목록 조회가 성공적으로 완료되었습니다.", null));
	}

	// TODO : 삭제까지 구현 할 것
}
