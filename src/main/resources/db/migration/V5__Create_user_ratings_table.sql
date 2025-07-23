CREATE TABLE user_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_user_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    meeting_id BIGINT NOT NULL,
    rating_score INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_rating (reviewer_id, target_user_id, meeting_id)
);