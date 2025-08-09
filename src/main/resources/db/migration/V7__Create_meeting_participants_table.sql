CREATE TABLE meeting_participants
(
    id                bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
    attendance_status tinyint(1) DEFAULT NULL,
    meeting_id        bigint NOT NULL,
    user_id           bigint NOT NULL,
    created_at        datetime DEFAULT NULL,

    FOREIGN KEY (meeting_id) REFERENCES meetings (id),
    UNIQUE (meeting_id, user_id)
);