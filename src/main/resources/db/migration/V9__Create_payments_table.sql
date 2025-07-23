CREATE TABLE payments
(
    id                bigint                         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    amount            int                            NOT NULL,
    created_at        datetime     DEFAULT NULL,
    meeting_id        bigint                         NOT NULL,
    paid_at           datetime     DEFAULT NULL,
    user_id           bigint                         NOT NULL,
    version           bigint       DEFAULT NULL,
    order_id          varchar(255) DEFAULT NULL,
    payment_method    varchar(255)                   NOT NULL,
    pg_transaction_id varchar(255) DEFAULT NULL,
    status            enum ('COMPLETED', 'REFUNDED') NOT NULL,

    UNIQUE KEY uq_payment_meeting_user (meeting_id, user_id)
);