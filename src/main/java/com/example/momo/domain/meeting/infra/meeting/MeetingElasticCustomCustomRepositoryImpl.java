package com.example.momo.domain.meeting.infra.meeting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meeting.domain.MeetingDocument;
import com.example.momo.domain.meeting.enums.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingElasticCustomCustomRepositoryImpl implements MeetingElasticCustomRepository {

	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public Page<MeetingDocument> getMeetings(String title, LocalDateTime meetingDate, MeetingStatus status,
		Integer categoryId, Pageable pageable) {

		Criteria criteria = new Criteria();

		if (title != null && !title.isBlank()) {
			criteria = criteria.and("title").contains(title);
		}

		if (meetingDate != null) {
			LocalDateTime start = meetingDate.toLocalDate().atStartOfDay();
			LocalDateTime end = start.plusDays(1);

			criteria = criteria.and("meetingDate").between(start, end);
		}

		if (status != null) {
			criteria = criteria.and("status").is(status.name());
		}

		if (categoryId != null) {
			criteria = criteria.and("categoryId").is(categoryId);
		}

		CriteriaQuery query = new CriteriaQuery(criteria, pageable);

		SearchHits<MeetingDocument> searchHits =
			elasticsearchOperations.search(query, MeetingDocument.class);

		List<MeetingDocument> content = searchHits.getSearchHits()
			.stream()
			.map(SearchHit::getContent)
			.collect(Collectors.toList());

		return new PageImpl<>(content, pageable, searchHits.getTotalHits());
	}
}
