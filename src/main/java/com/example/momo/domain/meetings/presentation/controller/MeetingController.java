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

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/meetings")
public class MeetingCoreController {

	private final MeetingService meetingCoreService;

	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createMeeting() {

		return ResponseEntity.ok(ApiResponse.success("모임 생성이 성공적으로 완료되었습니다.", null));
	}

	@GetMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<Void>> searchMeeting(@PathVariable Long meetingId) {

		return ResponseEntity.ok(ApiResponse.success("모임 조회가 성공적으로 완료되었습니다.", null));
	}

	@PutMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<Void>> updateMeeting(@PathVariable Long meetingId) {

		return ResponseEntity.ok(ApiResponse.success("모임 수정이 성공적으로 완료되었습니다.", null));
	}

	@PatchMapping("/{meetingId}")
	public ResponseEntity<ApiResponse<Void>> updateMeetingStatus(@PathVariable Long meetingId,
		@RequestBody MeetingStatus status) {

		return ResponseEntity.ok(ApiResponse.success("모임 상태 변경이 성공적으로 완료되었습니다.", null));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Void>> searchMeetings(
		@RequestParam Long categoryId,
		@RequestParam int page,
		@RequestParam int size,
		@RequestParam double latitude,
		@RequestParam double longitude) {

		return ResponseEntity.ok(ApiResponse.success("모임 목록 조회가 성공적으로 완료되었습니다.", null));
	}
}
