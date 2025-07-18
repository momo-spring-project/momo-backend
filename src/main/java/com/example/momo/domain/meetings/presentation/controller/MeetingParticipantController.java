package com.example.momo.domain.meetings.presentation.controller;

import com.example.momo.domain.meetings.application.MeetingParticipantService;
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

	private final MeetingParticipantService meetingParticipantService;

	// 모임 참가
	@PostMapping
	public ResponseEntity<ParticipantResponseDto> addParticipant(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long meetingId
	) {
		ParticipantResponseDto response = meetingParticipantService.addParticipant(userId, meetingId);
		return ResponseEntity.ok(response);
	}

	// 참가자 조회
	@GetMapping
	public ResponseEntity<List<Long>> getParticipants(
		@PathVariable Long meetingId
	) {
		List<Long> response = meetingParticipantService.getParticipants(meetingId);
		return ResponseEntity.ok(response);
	}

	// 참가자 취소
	@DeleteMapping
	public ResponseEntity<ParticipantResponseDto> cancelParticipant(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long meetingId
	) {
		ParticipantResponseDto response = meetingParticipantService.cancelParticipant(userId, meetingId);
		return ResponseEntity.ok(response);
	}
}