package com.example.momo.domain.meetings.infra;

import static com.example.momo.domain.meetings.domain.QMeeting.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.enums.MeetingStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingQueryRepositoryImpl implements MeetingQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Meeting> findMeetings(String title, LocalDateTime meetingDate, MeetingStatus status,
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
}
