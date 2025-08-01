package com.example.momo.domain.meeting.infra.meeting;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.example.momo.domain.meeting.domain.MeetingDocument;

public interface MeetingElasticRepository extends ElasticsearchRepository<MeetingDocument, String> {
}
