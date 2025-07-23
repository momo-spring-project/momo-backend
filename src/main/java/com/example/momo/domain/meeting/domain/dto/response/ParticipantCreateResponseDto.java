package com.example.momo.domain.meeting.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 비동기 처리시 결제 요청 완료 상태 확인용
@Getter
@AllArgsConstructor
public class ParticipantCreateResponseDto {
	private String status;
	private String message;
}