package com.example.momo.domain.user.application;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.user.application.dto.RegisterRequestDto;
import com.example.momo.domain.user.application.dto.UserAuthResponseDto;
import com.example.momo.domain.user.application.dto.UserFollowListResponseDto;
import com.example.momo.domain.user.application.dto.UserFollowResponseDto;
import com.example.momo.domain.user.application.dto.UserListResponseDto;
import com.example.momo.domain.user.application.dto.UserLocationResponseDto;
import com.example.momo.domain.user.application.dto.UserLocationUpdateRequestDto;
import com.example.momo.domain.user.application.dto.UserMeetingStatsDto;
import com.example.momo.domain.user.application.dto.UserNicknameUpdateRequestDto;
import com.example.momo.domain.user.application.dto.UserPasswordUpdateRequestDto;
import com.example.momo.domain.user.application.dto.UserRatingCreateRequestDto;
import com.example.momo.domain.user.application.dto.UserResponseDto;
import com.example.momo.domain.user.application.dto.WithdrawRequestDto;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserCategory;
import com.example.momo.domain.user.domain.UserFollow;
import com.example.momo.domain.user.domain.UserRating;
import com.example.momo.domain.user.domain.UserRepository;
import com.example.momo.domain.user.exception.UserErrorCode;
import com.example.momo.domain.user.exception.UserException;
import com.example.momo.domain.user.infra.rabbitmq.UserEventPublisher;
import com.example.momo.global.webclient.category.CategoryClient;
import com.example.momo.global.webclient.category.dto.CategoryClientResponseDto;
import com.example.momo.global.webclient.meeting.MeetingClient;
import com.example.momo.global.webclient.meeting.dto.MeetingClientResponseDto;
import com.example.momo.global.webclient.meeting.dto.ParticipantClientResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final CategoryClient categoryClient;
	private final BCryptPasswordEncoder passwordEncoder;
	private final MeetingClient meetingClient;
	private final UserEventPublisher userEventPublisher;

	@Override
	@Transactional
	public void registerUser(RegisterRequestDto request) {
		// 닉네임 중복 확인
		if (userRepository.existsByNickname(request.nickname())) {
			throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
		}

		// 이메일 중복 확인
		if (userRepository.existsByEmail(request.email())) {
			throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
		}

		User user = User.createUser(
			request.nickname(),
			request.email(),
			passwordEncoder.encode(request.password()),
			request.latitude(),
			request.longitude()
		);

		userRepository.save(user);

		// 회원가입 이벤트 발행
		userEventPublisher.publishUserRegistered(
			user.getId(),
			user.getNickname(),
			user.getEmail(),
			user.getLatitude(),
			user.getLongitude(),
			List.of() // 초기 카테고리는 빈 리스트
		);
	}

	@Override
	@Transactional
	public void withdrawUser(WithdrawRequestDto request, Long userId) {
		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		// 비밀번호가 있는 경우에만 검증 (소셜 로그인 사용자는 비밀번호가 null일 수 있음)
		if (user.getPassword() != null && !passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new UserException(UserErrorCode.PASSWORD_MISMATCH);
		}

		user.delete();

		// 탈퇴 이벤트 발행
		userEventPublisher.publishUserWithdrawn(user.getId(), user.getEmail(), user.getNickname());
	}

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
	public UserAuthResponseDto getUserByEmailForAuth(String email) {
		User user = userRepository.findByEmailAndIsDeletedFalse(email)
			.orElse(null);

		if (user == null) {
			return null;
		}

		// Auth 전용 DTO로 변환 (비밀번호 포함)
		return new UserAuthResponseDto(user);
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

		// 기존 카테고리 백업
		List<Integer> oldCategoryIds = user.getCategories().stream()
			.map(UserCategory::getCategoryId)
			.toList();

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
		String oldNickname = user.getNickname();

		if (userRepository.isDuplicateNickname(request.nickname(), userId)) {
			throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
		}
		user.updateNickname(request.nickname());
	}

	@Override
	@Transactional
	public UserLocationResponseDto updateUserLocation(Long userId, UserLocationUpdateRequestDto request) {
		User user = validateAndGetUser(userId);

		Double oldLatitude = user.getLatitude();
		Double oldLongitude = user.getLongitude();

		user.updateLocation(request.latitude(), request.longitude());

		return new UserLocationResponseDto(user);
	}

	// === 사용자 평가 및 점수 집계 로직 ===
	@Override
	@Transactional
	public void createUserRating(Long reviewerId, Long targetUserId, UserRatingCreateRequestDto request) {
		// 자기 자신 평가 방지
		if (reviewerId.equals(targetUserId)) {
			throw new UserException(UserErrorCode.CANNOT_RATE_SELF);
		}

		// 평가자 존재 확인
		User reviewer = validateAndGetUser(reviewerId);

		// 평가 대상자 존재 확인
		User targetUser = userRepository.findByIdAndIsDeletedFalse(targetUserId)
			.orElseThrow(() -> new UserException(UserErrorCode.TARGET_USER_NOT_FOUND));

		// 모임 존재 확인
		validateSameMeetingParticipants(reviewerId, targetUserId, request.meetingId());

		// 중복 평가 확인 - targetUser의 ratings 에서 확인
		boolean alreadyRated = targetUser.getRatings().stream()
			.anyMatch(rating ->
				rating.getReviewerId().equals(reviewerId) &&
					rating.getMeetingId().equals(request.meetingId())
			);

		if (alreadyRated) {
			throw new UserException(UserErrorCode.DUPLICATE_RATING);
		}

		// 평가 생성 및 targetUser의 ratings에 추가
		UserRating userRating = new UserRating(
			reviewerId,
			targetUserId,
			request.meetingId(),
			request.ratingScore()
		);

		// User 애그리거트를 통해 평가 추가
		targetUser.getRatings().add(userRating);

		// 평가 생성 이벤트 발행
		userEventPublisher.publishUserRated(
			reviewerId,
			targetUserId,
			request.meetingId(),
			request.ratingScore(),
			reviewer.getNickname(),
			targetUser.getNickname()
		);

		recalculateUserScore(targetUserId);
	}

	private void validateSameMeetingParticipants(Long reviewerId, Long targetUserId, Long meetingId) {
		// 참가자 ID 목록만 조회 (현재 Meeting API가 제공하는 방식)
		List<Long> participantIds = meetingClient.getParticipantIds(meetingId);

		// 디버그 로그 추가
		log.info("=== 모임 참가자 검증 시작 ===");
		log.info("meetingId: {}", meetingId);
		log.info("reviewerId: {}", reviewerId);
		log.info("targetUserId: {}", targetUserId);
		log.info("participantIds from API: {}", participantIds);

		if (participantIds == null || participantIds.isEmpty()) {
			log.error("참가자 목록이 비어있음!");
			throw new UserException(UserErrorCode.NOT_SAME_MEETING_PARTICIPANTS);
		}

		boolean reviewerParticipated = participantIds.contains(reviewerId);
		boolean targetParticipated = participantIds.contains(targetUserId);

		log.info("reviewerParticipated: {}", reviewerParticipated);
		log.info("targetParticipated: {}", targetParticipated);

		if (!reviewerParticipated || !targetParticipated) {
			log.error("참가 검증 실패!");
			throw new UserException(UserErrorCode.NOT_SAME_MEETING_PARTICIPANTS);
		}

		log.info("=== 모임 참가자 검증 완료 ===");
	}

	@Override
	@Transactional
	public void recalculateUserScore(Long userId) {
		User user = validateAndGetUser(userId);
		Double oldScore = user.getScore();

		// 1. 평점 점수 계산 (60%)
		double ratingScore = calculateRatingScore(user);

		// 2. MeetingClient를 통한 통계 조회
		UserMeetingStatsDto meetingStats = getUserMeetingStats(userId);

		// 3. 참석률 점수 계산 (30%)
		double attendanceScore = calculateAttendanceScore(meetingStats);

		// 4. 활동도 점수 계산 (10%)
		double activityScore = calculateActivityScore(meetingStats);

		// 5. 총 점수 계산
		double totalScore = ratingScore + attendanceScore + activityScore;

		// 6. 점수 업데이트
		user.updateScore(totalScore);
	}

	/**
	 * MeetingClient를 통해 사용자의 모임 참가 통계 조회
	 */
	private UserMeetingStatsDto getUserMeetingStats(Long userId) {
		// MeetingClient를 통해 사용자가 참가한 모임 목록 조회
		List<MeetingClientResponseDto> userMeetings = meetingClient.getMeetingsByUserId(userId);

		if (userMeetings == null || userMeetings.isEmpty()) {
			// 참가한 모임이 없는 경우 모든 값을 0으로 반환
			return new UserMeetingStatsDto(0L, 0L, 0L);
		}

		// 총 참가 모임 수
		long totalParticipation = userMeetings.size();

		// 최근 3개월 기준 날짜
		LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

		// 각 모임별로 출석 여부와 최근 참가 여부 확인
		long attendedMeetings = 0L;
		long recentParticipation = 0L;

		for (MeetingClientResponseDto meeting : userMeetings) {
			// 각 모임의 참가자 중에서 해당 사용자의 출석 정보 확인
			// 현재는 참가자 상세 정보를 개별 조회해야 함 (N+1 문제)
			ParticipantClientResponseDto userParticipant = meetingClient.getParticipant(meeting.getId(), userId);

			if (userParticipant != null) {
				// 출석 여부 확인
				if (userParticipant.isAttendanceStatus()) {
					attendedMeetings++;
				}
			}

			// 최근 3개월 참가 여부 확인 (모임 생성일 기준)
			if (meeting.getMeetingDate().isAfter(threeMonthsAgo)) {
				recentParticipation++;
			}
		}

		return new UserMeetingStatsDto(totalParticipation, attendedMeetings, recentParticipation);
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
	 */
	private double calculateAttendanceScore(UserMeetingStatsDto meetingStats) {
		if (meetingStats.totalParticipation() == 0) {
			throw new IllegalStateException("평가받은 사용자인데 참가한 모임이 없습니다. 데이터 정합성 오류!");
		}

		// 30점 만점으로 변환
		return meetingStats.getAttendanceRate() * 30.0;
	}

	/**
	 * 활동도 기반 점수 계산 (10%)
	 * 최근 3개월 모임 참가 횟수 기준
	 */
	private double calculateActivityScore(UserMeetingStatsDto meetingStats) {
		double monthlyAverage = meetingStats.getMonthlyAverage();

		// 활동도 점수 계산
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

		// 팔로우 생성 이벤트 발행
		userEventPublisher.publishUserFollowed(
			followerId,
			followingId,
			follower.getNickname(),
			following.getNickname()
		);
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
