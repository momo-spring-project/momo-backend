package com.example.momo.domain.meeting.infra;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.momo.domain.meeting.domain.MeetingDocument;
import com.example.momo.domain.meeting.enums.MeetingStatus;

public interface MeetingElasticCustomRepository {

	Page<MeetingDocument> getMeetings(String title, LocalDateTime meetingDate,
		MeetingStatus status, Integer categoryId,
		Pageable pageable);
}
