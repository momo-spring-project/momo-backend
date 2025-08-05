-- 사용자 아웃박스 이벤트 테이블
CREATE TABLE user_outbox_events
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    event_data    TEXT,
    published     BOOLEAN      NOT NULL DEFAULT FALSE,
    published_at  TIMESTAMP    NULL,
    retry_count   INT          NOT NULL DEFAULT 0,
    last_retry_at TIMESTAMP    NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);