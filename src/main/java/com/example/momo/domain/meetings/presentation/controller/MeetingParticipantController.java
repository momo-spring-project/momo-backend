package com.example.momo.domain.meetings.presentation.controller;

import com.example.momo.domain.meetings.application.MeetingParticipantService;
import com.example.momo.domain.meetings.presentation.dto.ParticipantResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings/{meetingId}/participants")
@RequiredArgsConstructor
public class MeetingParticipantController {

	private final MeetingParticipantService meetingParticipantService;

	// 유저 수정 예정
	// 모임 참가
	@PostMapping
	public ResponseEntity<ParticipantResponseDto> addParticipant(
		@PathVariable Long meetingId
	) {
		Long userId = 1L;
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

	@DeleteMapping
	public ResponseEntity<ParticipantResponseDto> cancelParticipant(
		@PathVariable Long meetingId
	) {
		Long userId = 1L;
		ParticipantResponseDto response = meetingParticipantService.cancelParticipant(userId, meetingId);
		return ResponseEntity.ok(response);
	}
}