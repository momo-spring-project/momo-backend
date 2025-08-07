package com.example.momo.domain.meeting.infra;

import static com.example.momo.domain.meeting.domain.QMeeting.*;
import static com.example.momo.domain.meeting.domain.QMeetingParticipant.*;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meeting.domain.QMeetingParticipant;
import com.querydsl.core.BooleanBuilder;
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

	@Override
	public Long countParticipants(Long userId, Long meetingId, Boolean attendance, LocalDateTime createdAt) {

		QMeetingParticipant participant = QMeetingParticipant.meetingParticipant;

		BooleanBuilder builder = new BooleanBuilder();

		// meetingId 0이면 전체 조회
		if(meetingId != 0L) {
			builder.and(participant.meetingId.eq(meetingId));
		}
		// attendance null 이면 전체 조회
		if(attendance != null) {
			builder.and(participant.attendanceStatus.eq(attendance));
		}

		builder.and(participant.userId.eq(userId));
		builder.and(participant.createdAt.after(createdAt));

		return queryFactory
			.select(participant.count())
			.from(participant)
			.where(builder)
			.fetchOne();
	}
}
