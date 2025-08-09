CREATE TABLE meeting_payment_outbox
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id   BIGINT       NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    published_at TIMESTAMP,
    published    BOOLEAN      NOT NULL DEFAULT FALSE
);