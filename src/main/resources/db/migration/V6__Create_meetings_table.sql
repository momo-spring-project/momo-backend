CREATE TABLE meetings
(
    id                         bigint                          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_id                int      DEFAULT NULL,
    current_participants_count int                             NOT NULL,
    is_deleted                 tinyint(1)                      NOT NULL,
    latitude                   double                          NOT NULL,
    longitude                  double                          NOT NULL,
    max_participants_count     int                             NOT NULL,
    min_enter_score            double                          NOT NULL,
    participation_fee          int                             NOT NULL,
    version                    int      DEFAULT NULL,
    created_at                 datetime DEFAULT NULL,
    deleted_at                 datetime DEFAULT NULL,
    host_user_id               bigint                          NOT NULL,
    meeting_date               datetime                        NOT NULL,
    meeting_end_date           datetime                        NOT NULL,
    updated_at                 datetime DEFAULT NULL,
    description                varchar(255)                    NOT NULL,
    location_name              varchar(255)                    NOT NULL,
    title                      varchar(255)                    NOT NULL,
    status                     enum ('FINISHED','IN_PROGRESS') NOT NULL,

    INDEX idx_meeting_deleted_category_status_date (is_deleted, category_id, status, meeting_date)
);