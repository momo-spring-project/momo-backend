package com.example.momo.domain.meeting.infra.participant;

import com.example.momo.domain.meeting.domain.QMeetingParticipant;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class MeetingParticipantQueryRepositoryImpl implements MeetingParticipantQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Long countParticipants(Long meetingId, Boolean attendance, LocalDateTime createdAt) {

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

		builder.and(participant.createdAt.after(createdAt));

		return queryFactory
			.select(participant.count())
			.from(participant)
			.where(builder)
			.fetchOne();
	}
}