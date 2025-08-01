package com.example.momo.domain.meeting.infra.meeting;

import static com.example.momo.domain.meeting.domain.QMeeting.*;
import static com.example.momo.domain.meeting.domain.QMeetingParticipant.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.meeting.domain.Meeting;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingQueryRepositoryImpl implements MeetingQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Meeting> findMeetingsByUserId(Long userId) {

		return queryFactory
			.selectFrom(meeting)
			.join(meeting.participants, meetingParticipant)
			.where(meetingParticipant.userId.eq(userId), meeting.isDeleted.isFalse())
			.fetch();
	}
}
