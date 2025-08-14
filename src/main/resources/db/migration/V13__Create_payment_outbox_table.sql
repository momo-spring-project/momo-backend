CREATE TABLE payment_outbox
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type     VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    routing_key    VARCHAR(255) NOT NULL,
    payload        MEDIUMTEXT   NOT NULL,
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME,
    published_at   DATETIME,
    published      BOOLEAN      NOT NULL DEFAULT FALSE,
    retry_count    INT          DEFAULT 0,
    correlation_id VARCHAR(255) UNIQUE,
    status         VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    next_retry_at  DATETIME,


    -- 핵심 인덱스: status + next_retry_at
    INDEX idx_status_next_retry (status, next_retry_at),

    -- PENDING 처리용
    INDEX idx_status_created (status, created_at),

    -- 정리 작업용
    INDEX idx_published_at (status, published_at)
)