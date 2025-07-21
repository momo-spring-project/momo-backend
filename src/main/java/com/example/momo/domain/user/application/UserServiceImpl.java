package com.example.momo.domain.user.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.categories.dto.CategoryResponseDto;
import com.example.momo.domain.categories.exception.CategoryException;
import com.example.momo.domain.categories.service.CategoryService;
import com.example.momo.domain.meetings.infra.MeetingParticipantJpaRepository;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserFollow;
import com.example.momo.domain.user.domain.UserRating;
import com.example.momo.domain.user.domain.dto.UserEmailUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserFollowInfoResponseDto;
import com.example.momo.domain.user.domain.dto.UserFollowListResponseDto;
import com.example.momo.domain.user.domain.dto.UserInfoResponseDto;
import com.example.momo.domain.user.domain.dto.UserNicknameUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserPasswordUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserRatingCreateRequestDto;
import com.example.momo.domain.user.exception.UserException;
import com.example.momo.domain.user.infra.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final CategoryService categoryService;
	private final BCryptPasswordEncoder passwordEncoder;
	private final MeetingParticipantJpaRepository meetingParticipantRepository;

	// === 사용자 정보 조회 ===
	@Override
	@Transactional(readOnly = true)
	public UserInfoResponseDto getUserById(Long userId) {
		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(UserException::userNotFound);
		return new UserInfoResponseDto(user);
	}

	@Override
	@Transactional(readOnly = true)
	public UserInfoResponseDto getCurrentUser(Long currentUserId) {
		return getUserById(currentUserId);
	}

	@Override
	@Transactional(readOnly = true)
	public User validateAndGetUser(Long userId) {
		return userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(UserException::userNotFound);
	}

	// === 사용자 정보 수정 ===
	@Override
	@Transactional
	public User updateUserCategories(Long userId, List<Integer> categoryIds) {
		User user = validateAndGetUser(userId);

		try {
			List<CategoryResponseDto> categories = categoryService.getCategories(categoryIds);

			if (categories.size() != categoryIds.size()) {
				throw UserException.invalidCategoryIds();
			}

			user.updateCategories(categoryIds);
			return user;

		} catch (CategoryException e) {
			throw UserException.invalidCategoryIds();
		}
	}

	@Override
	@Transactional
	public void updatePassword(Long userId, UserPasswordUpdateRequestDto request) {
		User user = validateAndGetUser(userId);

		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			throw UserException.passwordMismatch();
		}

		if (!request.newPassword().equals(request.confirmPassword())) {
			throw UserException.passwordConfirmMismatch();
		}

		user.updatePassword(passwordEncoder.encode(request.newPassword()));
	}

	@Override
	@Transactional
	public void updateNickname(Long userId, UserNicknameUpdateRequestDto request) {
		User user = validateAndGetUser(userId);

		if (userRepository.existsByNicknameAndIdNot(request.nickname(), userId)) {
			throw UserException.duplicateNickname();
		}
		user.updateNickname(request.nickname());
	}

	@Override
	@Transactional
	public void updateEmail(Long userId, UserEmailUpdateRequestDto request) {
		User user = validateAndGetUser(userId);

		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			throw UserException.passwordMismatch();
		}

		if (userRepository.existsByEmailAndIdNot(request.email(), userId)) {
			throw UserException.duplicateEmail();
		}

		user.updateEmail(request.email());
	}

	// === 사용자 평가 및 점수 집계 로직 ===
	@Override
	@Transactional
	public void createUserRating(Long reviewerId, Long targetUserId, UserRatingCreateRequestDto request) {
		// 1. 자기 자신 평가 방지
		if (reviewerId.equals(targetUserId)) {
			throw UserException.cannotRateSelf();
		}

		// 2. 평가자 존재 확인
		User reviewer = validateAndGetUser(reviewerId);

		// 3. 평가 대상자 존재 확인
		User targetUser = userRepository.findByIdAndIsDeletedFalse(targetUserId)
			.orElseThrow(UserException::targetUserNotFound);

		// 4. 모임 존재 확인
		meetingParticipantRepository.findById(request.meetingId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));

		// 5. 같은 모임 참가 여부 확인
		validateSameMeetingParticipants(reviewerId, targetUserId, request.meetingId());

		// 6. 중복 평가 확인 - targetUser의 ratings에서 확인
		boolean alreadyRated = targetUser.getRatings().stream()
			.anyMatch(rating ->
				rating.getReviewerId().equals(reviewerId) &&
					rating.getMeetingId().equals(request.meetingId())
			);

		if (alreadyRated) {
			throw UserException.duplicateRating();
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
		// 평가자가 해당 모임에 참가했는지 확인
		boolean reviewerParticipated = meetingParticipantRepository.existsByMeetingIdAndUserId(meetingId, reviewerId);

		// 평가 대상자가 해당 모임에 참가했는지 확인
		boolean targetParticipated = meetingParticipantRepository.existsByMeetingIdAndUserId(meetingId, targetUserId);

		if (!reviewerParticipated || !targetParticipated) {
			throw UserException.notSameMeetingParticipants();
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
	 */
	private double calculateRatingScore(User user) {
		if (user.getRatings().isEmpty()) {
			// 신규 사용자는 기본 점수 (3.0/5.0 기준으로 36점)
			return 36.0;
		}

		// 평균 평점 계산
		double averageRating = user.getRatings().stream()
			.mapToInt(UserRating::getRatingScore)
			.average()
			.orElse(3.0); // 기본값 3.0

		// 5점 만점을 60점 만점으로 변환
		return (averageRating / 5.0) * 60.0;
	}

	/**
	 * 참석률 기반 점수 계산 (30%)
	 * 신청한 모임 대비 실제 참석한 모임의 비율
	 */
	private double calculateAttendanceScore(Long userId) {
		// 사용자가 참가 신청한 총 모임 수
		long totalParticipations = meetingParticipantRepository.countByUserId(userId);

		if (totalParticipations == 0) {
			// 참가한 모임이 없는 신규 사용자는 기본 점수 (70% 가정)
			return 21.0;
		}

		// 실제 참석한 모임 수 (attendanceStatus = true)
		long attendedMeetings = meetingParticipantRepository.countByUserIdAndAttendanceStatusTrue(userId);

		// 참석률 계산
		double attendanceRate = (double)attendedMeetings / totalParticipations;

		// 30점 만점으로 변환
		return attendanceRate * 30.0;
	}

	/**
	 * 활동도 기반 점수 계산 (10%)
	 * 최근 3개월 모임 참가 횟수 기준
	 */
	private double calculateActivityScore(Long userId) {
		// 최근 3개월 기준 날짜
		LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

		// 최근 3개월간 참가한 모임 수
		long recentParticipations = meetingParticipantRepository.countByUserIdAndCreatedAtAfter(userId, threeMonthsAgo);

		// 월 평균 참가 횟수 계산
		double monthlyAverage = recentParticipations / 3.0;

		// 활동도 점수 계산
		if (monthlyAverage >= 2) {
			return 10.0; // 월 평균 2회 이상
		} else if (monthlyAverage >= 1) {
			return 7.0;  // 월 평균 1-2회
		} else {
			return 3.0;  // 월 평균 1회 미만
		}
	}

	// === 팔로우 기능 ===
	@Override
	@Transactional
	public void followUser(Long followerId, Long followingId) {
		// 1. 자기 자신 팔로우 방지
		if (followerId.equals(followingId)) {
			throw UserException.cannotFollowSelf();
		}

		// 2. 팔로워(나) 존재 확인
		User follower = validateAndGetUser(followerId);

		// 3. 팔로잉 대상 존재 확인
		User following = validateAndGetUser(followingId);

		// 4. 이미 팔로우했는지 확인
		boolean alreadyFollowing = follower.getFollowings().stream()
			.anyMatch(follow -> follow.getFollowingId().equals(followingId));

		if (alreadyFollowing) {
			throw UserException.alreadyFollowing();
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
		// 1. 자기 자신 언팔로우 방지 (사실상 불가능하지만 안전장치)
		if (followerId.equals(followingId)) {
			throw UserException.cannotFollowSelf();
		}

		// 2. 팔로워(나) 존재 확인
		User follower = validateAndGetUser(followerId);

		// 3. 팔로잉 대상 존재 확인
		User following = validateAndGetUser(followingId);

		// 4. 팔로우 관계 찾기
		UserFollow followToRemove = follower.getFollowings().stream()
			.filter(follow -> follow.getFollowingId().equals(followingId))
			.findFirst()
			.orElseThrow(UserException::notFollowing);

		// 5. 팔로우 관계 제거
		follower.getFollowings().remove(followToRemove);

		follower.decrementFollowingCount();     // 내 팔로잉 수 -1
		following.decrementFollowerCount();     // 상대방 팔로워 수 -1
	}

	@Override
	@Transactional(readOnly = true)
	public UserFollowListResponseDto getFollowings(Long userId, Pageable pageable) {
		// 1. 사용자 존재 확인 및 미리 집계된 총 개수 획득
		User user = validateAndGetUser(userId);
		int totalCount = user.getFollowingCount(); // 미리 집계된 값 사용!

		// 2. 팔로잉 목록 조회 (COUNT 쿼리 없음)
		Slice<User> followingsSlice = userRepository.findFollowingsByUserId(userId, pageable);

		List<UserFollowInfoResponseDto> followingsList = followingsSlice.getContent()
			.stream()
			.map(UserFollowInfoResponseDto::new)
			.toList();

		return new UserFollowListResponseDto(
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

		List<UserFollowInfoResponseDto> followersList = followersSlice.getContent()
			.stream()
			.map(UserFollowInfoResponseDto::new)
			.toList();

		return new UserFollowListResponseDto(
			followersList,
			totalCount,
			pageable.getPageNumber(),
			pageable.getPageSize()
		);
	}
}
