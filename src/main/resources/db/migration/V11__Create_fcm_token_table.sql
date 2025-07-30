CREATE TABLE fcm_token
(
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at    DATETIME DEFAULT NULL,
    user_id       BIGINT       NOT NULL,
    token         VARCHAR(512) NOT NULL,
    platform_type VARCHAR(255) NOT NULL,

    CONSTRAINT uq_user_token UNIQUE (user_id, token)
);