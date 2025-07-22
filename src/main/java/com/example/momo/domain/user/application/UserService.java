package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.dto.UserFollowListResponseDto;
import com.example.momo.domain.user.domain.dto.UserInfoResponseDto;
import com.example.momo.domain.user.domain.dto.UserLocationResponseDto;
import com.example.momo.domain.user.domain.dto.UserLocationUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserNicknameUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserPasswordUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserRatingCreateRequestDto;

public interface UserService {

	User validateAndGetUser(Long userId);

	UserInfoResponseDto getUserById(Long userId);

	UserInfoResponseDto getCurrentUser(Long CurrentUserId);

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
