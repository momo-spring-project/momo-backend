CREATE TABLE meeting_payment_outbox
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id   BIGINT       NOT NULL,
    event_uuid   VARCHAR(255) NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    published_at TIMESTAMP,
    published    BOOLEAN      NOT NULL DEFAULT FALSE,
    processed    BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_event_uuid UNIQUE (event_uuid)
);

CREATE INDEX idx_published_processed ON meeting_payment_outbox (published, processed);