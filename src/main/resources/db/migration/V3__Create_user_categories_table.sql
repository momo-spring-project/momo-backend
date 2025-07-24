CREATE TABLE user_categories (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id BIGINT NOT NULL,
     category_id INT NOT NULL,

     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);