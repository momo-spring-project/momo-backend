CREATE TABLE user_social
(
    id          bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     bigint                   DEFAULT NULL,
    provider_id varchar(255)             DEFAULT NULL,
    type        enum ('GOOGLE', 'NAVER') DEFAULT NULL,

    FOREIGN KEY (user_id) REFERENCES users (id)
);