package com.example.momo.domain.meeting.event.springEvents;

import com.example.momo.domain.meeting.domain.Meeting;

public class MeetingElasticEvents {

	public record Save(Meeting meeting, Long outboxId) {
	}

	public record Delete(Meeting meeting, Long outboxId) {
	}
}
