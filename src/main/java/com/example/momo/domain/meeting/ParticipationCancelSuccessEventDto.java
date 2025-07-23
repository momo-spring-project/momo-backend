package com.example.momo.domain.meeting;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipationCancelSuccessEventDto {
	private Long meetingId;
	private Long userId;
}
