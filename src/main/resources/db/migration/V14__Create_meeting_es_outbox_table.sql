-- meeting elasticsearch outbox
CREATE TABLE meeting_elasticsearch_outbox
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id   BIGINT      NOT NULL,
    event_type   VARCHAR(20) NOT NULL,
    published    BOOLEAN     NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP   NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);