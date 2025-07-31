package com.example.momo.domain.meeting.application.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingPagingResponseDto<T> {

	private List<T> data;
	private Long total;
	private int totalPage;
	private int currentPage;
}
