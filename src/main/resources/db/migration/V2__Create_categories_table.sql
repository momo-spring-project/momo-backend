CREATE TABLE categories
(
    id          INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) DEFAULT NULL,
    name        VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO categories (name, description)
VALUES ('music_concert', '음악 / 콘서트'),
       ('movie_drama', '영화 / 드라마'),
       ('reading', '독서'),
       ('yoga_pilates', '요가 / 필라테스'),
       ('running_marathon', '러닝 / 마라톤'),
       ('hiking', '등산'),
       ('fitness_crossfit', '헬스 / 크로스핏'),
       ('cycling', '자전거'),
       ('camping_car_camping', '캠핑 / 차박'),
       ('food_tour', '맛집 탐방'),
       ('overseas_travel', '해외 여행'),
       ('finance_investment', '제태크 / 투자'),
       ('coding_development', '코딩 / 개발'),
       ('language_study', '외국어 공부'),
       ('design_uiux', '디자인 / UIUX'),
       ('peer_friend_finder', '또래 친구 찾기'),
       ('pet_gathering', '반려동물 모임'),
       ('local_community', '지역 기반 모임'),
       ('after_work_beer', '퇴근 후 맥주 한잔'),
       ('job_community', '직무 커뮤니티');