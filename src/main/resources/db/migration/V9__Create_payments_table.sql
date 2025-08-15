CREATE TABLE payments
(
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    amount            INT          NOT NULL,
    created_at        DATETIME     DEFAULT NULL,
    meeting_id        BIGINT       NOT NULL,
    paid_at           DATETIME     DEFAULT NULL,
    failed_at         DATETIME     DEFAULT NULL,
    refunded_at       DATETIME     DEFAULT NULL,
    user_id           BIGINT       NOT NULL,
    version           BIGINT       DEFAULT NULL,
    order_id          VARCHAR(255) DEFAULT NULL,
    payment_method    VARCHAR(255) NOT NULL,
    pg_transaction_id VARCHAR(255) DEFAULT NULL,
    fail_reason       VARCHAR(255) DEFAULT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'EXPIRED') NOT NULL,

    UNIQUE KEY uq_payment_meeting_user (meeting_id, user_id)
);