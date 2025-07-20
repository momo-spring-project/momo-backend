package com.example.momo.domain.meetings.presentation.controller;

import com.example.momo.domain.meetings.application.MeetingParticipantService;
import com.example.momo.domain.meetings.presentation.dto.ParticipantAddResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings/{meetingId}/participants")
@RequiredArgsConstructor
public class MeetingParticipantController {

	private final MeetingParticipantService meetingParticipantService;

	@PostMapping
	public ResponseEntity<ParticipantAddResponseDto> addParticipant(
		@PathVariable Long meetingId
	) {
		Long userId = 1L;
		ParticipantAddResponseDto response = meetingParticipantService.addParticipant(userId, meetingId);
		return ResponseEntity.ok(response);
	}
}
