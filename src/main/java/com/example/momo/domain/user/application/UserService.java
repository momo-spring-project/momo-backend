package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.dto.RegisterRequestDto;
import com.example.momo.domain.user.domain.dto.UserAuthResponseDto;
import com.example.momo.domain.user.domain.dto.UserFollowListResponseDto;
import com.example.momo.domain.user.domain.dto.UserListResponseDto;
import com.example.momo.domain.user.domain.dto.UserLocationResponseDto;
import com.example.momo.domain.user.domain.dto.UserLocationUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserNicknameUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserPasswordUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserRatingCreateRequestDto;
import com.example.momo.domain.user.domain.dto.UserResponseDto;
import com.example.momo.domain.user.domain.dto.WithdrawRequestDto;

public interface UserService {

	/**
	 * 회원가입
	 */
	void registerUser(RegisterRequestDto request);

	/**
	 * 회원탈퇴
	 */
	void withdrawUser(WithdrawRequestDto request, Long userId);

	User validateAndGetUser(Long userId);

	/**
	 * 다중 사용자 정보 조회
	 *
	 * @param userIds 조회할 사용자 ID 목록
	 * @return 사용자 정보 DTO 목록 (존재하지 않는 사용자는 제외)
	 */
	List<UserListResponseDto> getUsersByIds(List<Long> userIds);

	/**
	 * 사용자 존재 여부 확인
	 *
	 * @param userIds 확인할 사용자 ID 목록
	 * @return 존재하는 사용자 ID 목록
	 */
	List<Long> getExistingUserIds(List<Long> userIds);

	/**
	 * 이메일로 사용자 정보 조회
	 *
	 * @param email 조회할 사용자 이메일
	 * @return 사용자 정보 DTO (존재하지 않으면 null)
	 */
	UserAuthResponseDto getUserByEmailForAuth(String email);

	UserResponseDto getUserById(Long userId);

	UserResponseDto getMyProfile(Long CurrentUserId);

	/**
	 * 카테고리, 위도, 경도 기반으로 사용자를 필터링하여 조회
	 *
	 * @param categoryIds 관심 카테고리 ID 목록 (null이면 필터링 안함)
	 * @param latitude 위도 (null이면 필터링 안함)
	 * @param longitude 경도 (null이면 필터링 안함)
	 * @return 필터링된 사용자 목록
	 */
	List<UserListResponseDto> getUsersByLocationAndCategory(
		List<Integer> categoryIds,
		Double latitude,
		Double longitude
	);

	User updateUserCategories(Long userId, List<Integer> categoryIds);

	void updatePassword(Long userId, UserPasswordUpdateRequestDto request);

	void updateNickname(Long userId, UserNicknameUpdateRequestDto request);

	UserLocationResponseDto updateUserLocation(Long userId, UserLocationUpdateRequestDto request);

	void createUserRating(Long reviewerId, Long targetUserId, UserRatingCreateRequestDto request);

	void recalculateUserScore(Long userId);

	void followUser(Long followerId, Long followingId);

	/**
	 * 특정 사용자가 팔로잉하는 사용자 목록 조회 (미리 집계된 totalCount 활용)
	 */
	UserFollowListResponseDto getFollowings(Long userId, Pageable pageable);

	/**
	 * 특정 사용자를 팔로우하는 사용자 목록 조회 (미리 집계된 totalCount 활용)
	 */
	UserFollowListResponseDto getFollowers(Long userId, Pageable pageable);

	/**
	 * 팔로우 관계 삭제 (물리 삭제)
	 * @param followerId 팔로워 ID
	 * @param followingId 팔로잉 대상 ID
	 */
	void unfollowUser(Long followerId, Long followingId);
}
