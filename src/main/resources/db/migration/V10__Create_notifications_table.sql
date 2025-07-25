CREATE TABLE notifications
(
    id         bigint       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at datetime DEFAULT NULL,
    meeting_id bigint       NOT NULL,
    user_id    bigint       NOT NULL,
    notification_type  varchar(255) NOT NULL,
    content    varchar(255) NOT NULL,

    INDEX idx_notification_user_id_id_desc (user_id, id DESC)
);