DROP TABLE IF EXISTS payment_outbox;

CREATE TABLE payment_outbox
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type     VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    routing_key    VARCHAR(255) NOT NULL,
    payload        TEXT         NOT NULL,
    created_at     DATETIME     NOT NULL,
    published_at   DATETIME,
    published      BOOLEAN      NOT NULL DEFAULT FALSE,
    retry_count    INT          DEFAULT 0,
    correlation_id VARCHAR(255) UNIQUE,
    status         VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT
);
