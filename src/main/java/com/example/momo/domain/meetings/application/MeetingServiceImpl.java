package com.example.momo.domain.meetings.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

	private final MeetingCoreService meetingCoreService;
	private final MeetingParticipantService meetingParticipantService;

}
