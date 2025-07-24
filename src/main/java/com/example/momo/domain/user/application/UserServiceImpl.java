package com.example.momo.domain.user.application;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meeting.infra.participant.MeetingParticipantJpaRepository;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserFollow;
import com.example.momo.domain.user.domain.UserRating;
import com.example.momo.domain.user.domain.UserRepository;
import com.example.momo.domain.user.domain.dto.UserFollowListResponseDto;
import com.example.momo.domain.user.domain.dto.UserFollowResponseDto;
import com.example.momo.domain.user.domain.dto.UserListResponseDto;
import com.example.momo.domain.user.domain.dto.UserLocationResponseDto;
import com.example.momo.domain.user.domain.dto.UserLocationUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserNicknameUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserPasswordUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserRatingCreateRequestDto;
import com.example.momo.domain.user.domain.dto.UserResponseDto;
import com.example.momo.domain.user.exception.UserErrorCode;
import com.example.momo.domain.user.exception.UserException;
import com.example.momo.global.infrastructure.client.category.CategoryClient;
import com.example.momo.global.infrastructure.client.category.dto.CategoryClientResponseDto;
import com.example.momo.global.infrastructure.client.meeting.MeetingClient;
import com.example.momo.global.infrastructure.client.meeting.dto.ParticipantClientResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final CategoryClient categoryClient;
	private final BCryptPasswordEncoder passwordEncoder;
	private final MeetingClient meetingClient;
	private final MeetingParticipantJpaRepository meetingParticipantRepository;

	// === 사용자 정보 조회 ===
	@Override
	@Transactional(readOnly = true)
	public UserResponseDto getUserById(Long userId) {
		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
		return new UserResponseDto(user);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserListResponseDto> getUsersByIds(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return List.of();
		}

		List<User> users = userRepository.findAllByIdInAndIsDeletedFalse(userIds);

		return users.stream()
			.map(UserListResponseDto::new)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getExistingUserIds(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return List.of();
		}

		return userRepository.findExistingUserIds(userIds);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponseDto getMyProfile(Long currentUserId) {
		return getUserById(currentUserId);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponseDto getUserByEmail(String email) {
		return userRepository.findByEmailAndIsDeletedFalse(email)
			.map(UserResponseDto::new)
			.orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserListResponseDto> getUsersByLocationAndCategory(
		List<Integer> categoryIds,
		Double latitude,
		Double longitude
	) {
		List<User> users = userRepository.getUsersByLocationAndCategory(categoryIds, latitude, longitude);

		return users.stream()
			.map(UserListResponseDto::new)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public User validateAndGetUser(Long userId) {
		return userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
	}

	// === 사용자 정보 수정 ===
	@Override
	@Transactional
	public User updateUserCategories(Long userId, List<Integer> categoryIds) {
		User user = validateAndGetUser(userId);

		List<CategoryClientResponseDto> allCategories = categoryClient.getCategories();

		if (allCategories == null || allCategories.isEmpty()) {
			throw new UserException(UserErrorCode.INVALID_CATEGORY_IDS);
		}

		List<Integer> validCategoryIds = allCategories.stream()
			.map(CategoryClientResponseDto::getId)
			.toList();

		boolean allValid = new HashSet<>(validCategoryIds).containsAll(categoryIds);

		if (!allValid) {
			throw new UserException(UserErrorCode.INVALID_CATEGORY_IDS);
		}

		user.updateCategories(categoryIds);
		return user;
	}

	@Override
	@Transactional
	public void updatePassword(Long userId, UserPasswordUpdateRequestDto request) {
		User user = validateAndGetUser(userId);

		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			throw new UserException(UserErrorCode.PASSWORD_MISMATCH);
		}

		if (!request.newPassword().equals(request.confirmPassword())) {
			throw new UserException(UserErrorCode.PASSWORD_CONFIRM_MISMATCH);
		}

		user.updatePassword(passwordEncoder.encode(request.newPassword()));
	}

	@Override
	@Transactional
	public void updateNickname(Long userId, UserNicknameUpdateRequestDto request) {
		User user = validateAndGetUser(userId);

		if (userRepository.isDuplicateNickname(request.nickname(), userId)) {
			throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
		}
		user.updateNickname(request.nickname());
	}

	@Override
	@Transactional
	public UserLocationResponseDto updateUserLocation(Long userId, UserLocationUpdateRequestDto request) {
		User user = validateAndGetUser(userId);

		user.updateLocation(request.latitude(), request.longitude());

		return new UserLocationResponseDto(user);
	}

	// === 사용자 평가 및 점수 집계 로직 ===
	@Override
	@Transactional
	public void createUserRating(Long reviewerId, Long targetUserId, UserRatingCreateRequestDto request) {
		// 1. 자기 자신 평가 방지
		if (reviewerId.equals(targetUserId)) {
			throw new UserException(UserErrorCode.CANNOT_RATE_SELF);
		}

		// 2. 평가자 존재 확인
		validateAndGetUser(reviewerId);

		// 3. 평가 대상자 존재 확인
		User targetUser = userRepository.findByIdAndIsDeletedFalse(targetUserId)
			.orElseThrow(() -> new UserException(UserErrorCode.TARGET_USER_NOT_FOUND));

		// 4. 모임 존재 확인
		validateSameMeetingParticipants(reviewerId, targetUserId, request.meetingId());

		// 5. 같은 모임 참가 여부 확인
		validateSameMeetingParticipants(reviewerId, targetUserId, request.meetingId());

		// 6. 중복 평가 확인 - targetUser의 ratings 에서 확인
		boolean alreadyRated = targetUser.getRatings().stream()
			.anyMatch(rating ->
				rating.getReviewerId().equals(reviewerId) &&
					rating.getMeetingId().equals(request.meetingId())
			);

		if (alreadyRated) {
			throw new UserException(UserErrorCode.DUPLICATE_RATING);
		}

		// 7. 평가 생성 및 targetUser의 ratings에 추가
		UserRating userRating = new UserRating(
			reviewerId,
			targetUserId,
			request.meetingId(),
			request.ratingScore()
		);

		// User 애그리거트를 통해 평가 추가
		targetUser.getRatings().add(userRating);
		recalculateUserScore(targetUserId);
	}

	private void validateSameMeetingParticipants(Long reviewerId, Long targetUserId, Long meetingId) {
		List<ParticipantClientResponseDto> participants = meetingClient.getParticipants(meetingId);

		if (participants == null || participants.isEmpty()) {
			throw new UserException(UserErrorCode.NOT_SAME_MEETING_PARTICIPANTS);
		}

		List<Long> participantUserIds = participants.stream()
			.map(ParticipantClientResponseDto::getUserId)
			.toList();

		boolean reviewerParticipated = participantUserIds.contains(reviewerId);
		boolean targetParticipated = participantUserIds.contains(targetUserId);

		if (!reviewerParticipated || !targetParticipated) {
			throw new UserException(UserErrorCode.NOT_SAME_MEETING_PARTICIPANTS);
		}
	}

	@Override
	@Transactional
	public void recalculateUserScore(Long userId) {
		User user = validateAndGetUser(userId);

		// 1. 평점 점수 계산 (60%)
		double ratingScore = calculateRatingScore(user);

		// 2. 참석률 점수 계산 (30%)
		double attendanceScore = calculateAttendanceScore(userId);

		// 3. 활동도 점수 계산 (10%)
		double activityScore = calculateActivityScore(userId);

		// 4. 총 점수 계산
		double totalScore = ratingScore + attendanceScore + activityScore;

		// 5. 점수 업데이트
		user.updateScore(totalScore);
	}

	/**
	 * 평점 기반 점수 계산 (60%)
	 * 평점 4.5/5.0 → (4.5/5.0) * 60 = 54점
	 * 주의: 이 메서드는 평가가 있는 사용자에 대해서만 호출됨
	 */
	private double calculateRatingScore(User user) {
		// 평가받은 사용자라면 ratings가 있어야 함
		if (user.getRatings().isEmpty()) {
			throw new IllegalStateException("평가 점수를 계산하려 하는데 평가가 없습니다!");
		}

		// 평균 평점 계산
		double averageRating = user.getRatings().stream()
			.mapToInt(UserRating::getRatingScore)
			.average()
			.orElseThrow(() -> new IllegalStateException("평가 데이터가 비어있습니다!"));

		// 5점 만점을 60점 만점으로 변환
		return (averageRating / 5.0) * 60.0;
	}

	/**
	 * 참석률 기반 점수 계산 (30%)
	 * 신청한 모임 대비 실제 참석한 모임의 비율
	 * 주의: 이 메서드는 평가를 받을 수 있는 사용자(= 최소 1개 모임 참가)에 대해서만 호출됨
	 */
	private double calculateAttendanceScore(Long userId) {
		// 사용자가 참가 신청한 총 모임 수
		long totalParticipation = meetingParticipantRepository.countByUserId(userId);

		// 평가받을 수 있다는 것은 최소 1개 모임은 참가했다는 의미
		// 만약 0이면 로직 오류
		if (totalParticipation == 0) {
			throw new IllegalStateException("평가받은 사용자인데 참가한 모임이 없습니다. 데이터 정합성 오류!");
		}

		// 실제 참석한 모임 수 (attendanceStatus = true)
		long attendedMeetings = meetingParticipantRepository.countByUserIdAndAttendanceStatusTrue(userId);

		// 참석률 계산
		double attendanceRate = (double)attendedMeetings / totalParticipation;

		// 30점 만점으로 변환
		return attendanceRate * 30.0;
	}

	/**
	 * 활동도 기반 점수 계산 (10%)
	 * 최근 3개월 모임 참가 횟수 기준
	 * 주의: 이 메서드는 모임 참가 기록이 있는 사용자에 대해서만 호출됨
	 */
	private double calculateActivityScore(Long userId) {
		// 최근 3개월 기준 날짜
		LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

		// 최근 3개월간 참가한 모임 수
		long recentParticipation = meetingParticipantRepository
			.countByUserIdAndCreatedAtAfter(userId, threeMonthsAgo);

		// 월 평균 참가 횟수 계산
		double monthlyAverage = recentParticipation / 3.0;

		// 활동도 점수 계산 (실제 데이터만으로)
		if (monthlyAverage >= 2.0) {
			return 10.0; // 월 평균 2회 이상
		} else if (monthlyAverage >= 1.0) {
			return 7.0;  // 월 평균 1-2회
		} else {
			return 3.0;  // 월 평균 1회 미만 (0회 포함)
		}
	}

	// === 팔로우 기능 ===
	@Override
	@Transactional
	public void followUser(Long followerId, Long followingId) {
		// 1. 자기 자신 팔로우 방지
		if (followerId.equals(followingId)) {
			throw new UserException(UserErrorCode.CANNOT_FOLLOW_SELF);
		}

		// 2. 팔로워(나) 존재 확인
		User follower = validateAndGetUser(followerId);

		// 3. 팔로잉 대상 존재 확인
		User following = validateAndGetUser(followingId);

		// 4. 이미 팔로우했는지 확인
		boolean alreadyFollowing = follower.getFollowings().stream()
			.anyMatch(follow -> follow.getFollowingId().equals(followingId));

		if (alreadyFollowing) {
			throw new UserException(UserErrorCode.ALREADY_FOLLOWING);
		}

		// 5. 팔로우 관계 생성 및 추가
		UserFollow userFollow = new UserFollow(followerId, followingId);
		follower.getFollowings().add(userFollow);

		follower.incrementFollowingCount();     // 내 팔로잉 수 +1
		following.incrementFollowerCount();     // 상대방 팔로워 수 +1
	}

	@Override
	@Transactional
	public void unfollowUser(Long followerId, Long followingId) {
		if (followerId.equals(followingId)) {
			throw new UserException(UserErrorCode.CANNOT_FOLLOW_SELF);
		}

		User follower = validateAndGetUser(followerId);
		User following = validateAndGetUser(followingId);

		boolean isFollowing = follower.getFollowings().stream()
			.anyMatch(follow -> follow.getFollowingId().equals(followingId));

		if (!isFollowing) {
			throw new UserException(UserErrorCode.NOT_FOLLOWING);
		}

		int deletedCount = userRepository.deleteUserFollow(followerId, followingId);
		if (deletedCount == 0) {
			throw new UserException(UserErrorCode.NOT_FOLLOWING);
		}

		follower.decrementFollowingCount();
		following.decrementFollowerCount();
	}

	@Override
	@Transactional(readOnly = true)
	public UserFollowListResponseDto getFollowings(Long userId, Pageable pageable) {
		// 1. 사용자 존재 확인 및 미리 집계된 총 개수 획득
		User user = validateAndGetUser(userId);
		int totalCount = user.getFollowingCount(); // 미리 집계된 값 사용!

		// 2. 팔로잉 목록 조회 (COUNT 쿼리 없음)
		Slice<User> followingsSlice = userRepository.findFollowingsByUserId(userId, pageable);

		List<UserFollowResponseDto> followingsList = followingsSlice.getContent()
			.stream()
			.map(UserFollowResponseDto::new)
			.toList();

		return UserFollowListResponseDto.of(
			followingsList,
			totalCount,
			pageable.getPageNumber(),
			pageable.getPageSize()
		);
	}

	@Override
	@Transactional(readOnly = true)
	public UserFollowListResponseDto getFollowers(Long userId, Pageable pageable) {
		// 1. 사용자 존재 확인 및 미리 집계된 총 개수 획득
		User user = validateAndGetUser(userId);
		int totalCount = user.getFollowerCount(); // 미리 집계된 값 사용!

		// 2. 팔로워 목록 조회 (COUNT 쿼리 없음)
		Slice<User> followersSlice = userRepository.findFollowersByUserId(userId, pageable);

		List<UserFollowResponseDto> followersList = followersSlice.getContent()
			.stream()
			.map(UserFollowResponseDto::new)
			.toList();

		// 3. 정적 팩토리 메서드 사용으로 변경
		return UserFollowListResponseDto.of(
			followersList,
			totalCount,
			pageable.getPageNumber(),
			pageable.getPageSize()
		);
	}
}
