package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.example.momo.domain.user.application.dto.RegisterRequestDto;
import com.example.momo.domain.user.application.dto.UserAuthResponseDto;
import com.example.momo.domain.user.application.dto.UserFollowListResponseDto;
import com.example.momo.domain.user.application.dto.UserListResponseDto;
import com.example.momo.domain.user.application.dto.UserLocationResponseDto;
import com.example.momo.domain.user.application.dto.UserLocationUpdateRequestDto;
import com.example.momo.domain.user.application.dto.UserNicknameUpdateRequestDto;
import com.example.momo.domain.user.application.dto.UserPasswordUpdateRequestDto;
import com.example.momo.domain.user.application.dto.UserRatingCreateRequestDto;
import com.example.momo.domain.user.application.dto.UserResponseDto;
import com.example.momo.domain.user.application.dto.WithdrawRequestDto;
import com.example.momo.domain.user.domain.User;

/**
 * 사용자 서비스 인터페이스
 * 주요 기능별 분류:
 * 1. 회원 관리 (가입, 탈퇴, 검증)
 * 2. 사용자 정보 조회 (단일, 다중, 검색)
 * 3. 사용자 정보 수정 (개인정보, 위치, 카테고리)
 * 4. 팔로우 시스템 (팔로우/언팔로우/목록)
 * 5. 평가 시스템 (평가 생성 및 점수 계산)
 */
public interface UserService {

	// ==================== 1. 회원 관리 ====================

	void registerUser(RegisterRequestDto request);

	void withdrawUser(WithdrawRequestDto request, Long userId);

	User validateAndGetUser(Long userId);

	// ==================== 2. 사용자 정보 조회 ====================

	UserResponseDto getMyProfile(Long currentUserId);

	UserResponseDto getUserById(Long userId);

	List<UserListResponseDto> getUsersByIds(List<Long> userIds);

	List<Long> getExistingUserIds(List<Long> userIds);

	/**
	 * 카테고리와 위치 기반 사용자 필터링 검색
	 * - 점수 높은 순으로 정렬
	 * - 10km 반경 내 검색
	 */
	List<UserListResponseDto> getUsersByLocationAndCategory(
		List<Integer> categoryIds,
		Double latitude,
		Double longitude
	);

	// Auth 도메인 전용 - 이메일로 사용자 조회
	UserAuthResponseDto getUserByEmailForAuth(String email);

	// ==================== 3. 사용자 정보 수정 ====================

	User updateUserCategories(Long userId, List<Integer> categoryIds);

	void updatePassword(Long userId, UserPasswordUpdateRequestDto request);

	void updateNickname(Long userId, UserNicknameUpdateRequestDto request);

	UserLocationResponseDto updateUserLocation(Long userId, UserLocationUpdateRequestDto request);

	// ==================== 4. 팔로우 시스템 ====================

	void followUser(Long followerId, Long followingId);

	void unfollowUser(Long followerId, Long followingId);

	UserFollowListResponseDto getFollowings(Long userId, Pageable pageable);

	UserFollowListResponseDto getFollowers(Long userId, Pageable pageable);

	// ==================== 5. 평가 시스템 ====================

	/**
	 * 사용자 평가 등록
	 * - 자기 자신 평가 방지
	 * - 같은 모임 참가자만 평가 가능
	 * - 중복 평가 방지
	 * - 평가 후 사용자 점수 재계산
	 */
	void createUserRating(Long reviewerId, Long targetUserId, UserRatingCreateRequestDto request);

	/**
	 * 사용자 점수 재계산
	 * - 평점 점수 (60%)
	 * - 참석률 점수 (30%)
	 * - 활동도 점수 (10%)
	 */
	void recalculateUserScore(Long userId);
}
