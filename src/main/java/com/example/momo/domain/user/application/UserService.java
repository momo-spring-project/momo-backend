package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.dto.UserEmailUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserFollowInfoResponseDto;
import com.example.momo.domain.user.domain.dto.UserInfoResponseDto;
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

	void updateEmail(Long userId, UserEmailUpdateRequestDto request);

	void createUserRating(Long reviewerId, Long targetUserId, UserRatingCreateRequestDto request);

	void recalculateUserScore(Long userId);

	void followUser(Long followerId, Long followingId);

	void unfollowUser(Long followerId, Long followingId);

	/**
	 * 특정 사용자가 팔로잉하는 사용자 목록 조회
	 */
	List<UserFollowInfoResponseDto> getFollowings(Long userId, Pageable pageable);

	/**
	 * 특정 사용자를 팔로우하는 사용자 목록 조회
	 */
	List<UserFollowInfoResponseDto> getFollowers(Long userId, Pageable pageable);
}
