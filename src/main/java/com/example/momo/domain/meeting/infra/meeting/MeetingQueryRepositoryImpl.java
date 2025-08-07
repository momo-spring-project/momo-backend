package com.example.momo.domain.meeting.infra.meeting;

import static com.example.momo.domain.meeting.domain.QMeeting.*;
import static com.example.momo.domain.meeting.domain.QMeetingParticipant.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.momo.domain.meeting.domain.QMeetingParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingQueryRepositoryImpl implements MeetingQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Meeting> getMeetings(String title, LocalDateTime meetingDate, MeetingStatus status, Integer categoryId,
		Pageable pageable) {

		BooleanBuilder builder = new BooleanBuilder();

		if (!title.trim().isEmpty()) {
			builder.and(meeting.title.like("%" + title.trim() + "%"));
		}

		if (meetingDate != null) {
			LocalDate date = meetingDate.toLocalDate();
			LocalDateTime start = date.atStartOfDay();
			LocalDateTime end = start.plusDays(1);

			builder.and(meeting.meetingDate.goe(start).and(meeting.meetingDate.lt(end)));
		}

		if (status != null) {
			builder.and(meeting.status.eq(status));
		}

		if (categoryId != null) {
			builder.and(meeting.categoryId.eq(categoryId));
		}

		builder.and(meeting.isDeleted.eq(false));

		List<Meeting> meetingContent = queryFactory
			.selectFrom(meeting)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(meeting.meetingDate.desc())
			.fetch();

		Long total = Optional.ofNullable(queryFactory
			.select(meeting.count())
			.from(meeting)
			.where(builder)
			.fetchOne()).orElse(0L);

		return new PageImpl<>(meetingContent, pageable, total);
	}

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
