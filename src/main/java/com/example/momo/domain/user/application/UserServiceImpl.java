package com.example.momo.domain.user.application;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
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
import com.example.momo.domain.user.domain.UserRating;
import com.example.momo.domain.user.domain.UserRepository;
import com.example.momo.domain.user.event.springEvent.UserEvents;
import com.example.momo.domain.user.exception.UserErrorCode;
import com.example.momo.domain.user.exception.UserException;
import com.example.momo.global.webclient.category.CategoryClient;
import com.example.momo.global.webclient.category.dto.CategoryClientResponseDto;
import com.example.momo.global.webclient.meeting.MeetingClient;
import com.example.momo.global.webclient.meeting.dto.MeetingClientResponseDto;
import com.example.momo.global.webclient.meeting.dto.ParticipantClientResponseDto;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 서비스 구현체
 * 주요 특징:
 * - 도메인 중심 설계: User 애그리거트를 통한 비즈니스 로직 처리
 * - 이벤트 기반 아키텍처: 아웃박스 패턴 + 스프링 이벤트
 * - 성능 최적화: Slice 사용, 미리 집계된 카운트 활용
 * - 안전한 예외 처리: 상세한 에러 메시지와 로깅
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final CategoryClient categoryClient;
	private final BCryptPasswordEncoder passwordEncoder;
	private final MeetingClient meetingClient;
	private final UserOutboxService userOutboxService;
	private final ApplicationEventPublisher eventPublisher;
	private final EntityManager entityManager;

	// ==================== 1. 회원 관리 ====================

	@Override
	@Transactional
	public void registerUser(RegisterRequestDto request) {
		log.info("회원가입 시작: email={}, nickname={}", request.email(), request.nickname());

		// 1. 닉네임 중복 확인
		if (userRepository.existsByNickname(request.nickname())) {
			throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
		}

		// 2. 이메일 중복 확인
		if (userRepository.existsByEmail(request.email())) {
			throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
		}

		// 3. 사용자 생성 및 저장
		User user = User.createUser(
			request.nickname(),
			request.email(),
			passwordEncoder.encode(request.password()),
			request.latitude(),
			request.longitude()
		);

		userRepository.save(user);
		log.info("회원가입 완료: userId={}, email={}", user.getId(), request.email());
	}

	@Override
	@Transactional
	public void withdrawUser(WithdrawRequestDto request, Long userId) {
		log.info("회원탈퇴 시작: userId={}", userId);

		// 1. 사용자 조회
		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		// 2. 비밀번호 검증 (소셜 로그인 사용자는 비밀번호가 null일 수 있음)
		if (user.getPassword() != null && !passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new UserException(UserErrorCode.PASSWORD_MISMATCH);
		}

		// 3. 논리 삭제 처리
		user.delete();

		// 4. 아웃박스 이벤트 저장 (다른 도메인 연동용)
		userOutboxService.saveUserWithdrawnEvent(
			user.getId(),
			user.getEmail(),
			user.getNickname()
		);

		// 5. 스프링 이벤트 발행 (애플리케이션 내부 처리용)
		eventPublisher.publishEvent(
			new UserEvents.Withdrawn(
				user.getId(),
				user.getEmail(),
				user.getNickname()
			)
		);

		log.info("회원탈퇴 완료: userId={}, email={}", userId, user.getEmail());
	}

	@Override
	@Transactional(readOnly = true)
	public User validateAndGetUser(Long userId) {
		return userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
	}

	// ==================== 2. 사용자 정보 조회 ====================

	@Override
	@Transactional(readOnly = true)
	public UserResponseDto getUserById(Long userId) {
		log.debug("사용자 정보 조회: userId={}", userId);

		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		return new UserResponseDto(user);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponseDto getMyProfile(Long currentUserId) {
		log.debug("내 프로필 조회: userId={}", currentUserId);
		return getUserById(currentUserId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserListResponseDto> getUsersByIds(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			log.debug("사용자 ID 목록이 비어있음");
			return List.of();
		}

		log.debug("다중 사용자 조회: userIds={}", userIds);

		List<User> users = userRepository.findAllByIdInAndIsDeletedFalse(userIds);

		log.debug("다중 사용자 조회 결과: 요청={}, 조회={}", userIds.size(), users.size());
		return users.stream()
			.map(UserListResponseDto::new)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getExistingUserIds(List<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			log.debug("확인할 사용자 ID 목록이 비어있음");
			return List.of();
		}

		log.debug("사용자 존재 여부 확인: userIds={}", userIds);

		List<Long> existingIds = userRepository.findExistingUserIds(userIds);

		log.debug("존재하는 사용자 ID: 요청={}, 존재={}", userIds.size(), existingIds.size());
		return existingIds;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserListResponseDto> getUsersByLocationAndCategory(
		List<Integer> categoryIds,
		Double latitude,
		Double longitude
	) {
		log.debug("사용자 필터링 검색: categoryIds={}, latitude={}, longitude={}",
			categoryIds, latitude, longitude);

		List<User> users = userRepository.getUsersByLocationAndCategory(categoryIds, latitude, longitude);

		log.debug("필터링 검색 결과: {}명", users.size());
		return users.stream()
			.map(UserListResponseDto::new)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public UserAuthResponseDto getUserByEmailForAuth(String email) {
		log.debug("Auth용 이메일로 사용자 조회: email={}", email);

		User user = userRepository.findByEmailAndIsDeletedFalse(email)
			.orElse(null);

		if (user == null) {
			log.debug("해당 이메일의 사용자가 존재하지 않음: email={}", email);
			return null;
		}

		// Auth 전용 DTO로 변환 (비밀번호 포함)
		return new UserAuthResponseDto(user);
	}

	// ==================== 3. 사용자 정보 수정 ====================

	@Override
	@Transactional
	public User updateUserCategories(Long userId, List<Integer> categoryIds) {
		log.info("관심 카테고리 수정: userId={}, categoryIds={}", userId, categoryIds);

		User user = validateAndGetUser(userId);

		// 1. 기존 카테고리 백업 (로깅용)
		List<Integer> oldCategoryIds = user.getCategories().stream()
			.map(UserCategory::getCategoryId)
			.toList();

		// 2. 카테고리 유효성 검증
		List<CategoryClientResponseDto> allCategories = categoryClient.getCategories();
		if (allCategories == null || allCategories.isEmpty()) {
			log.error("카테고리 목록 조회 실패");
			throw new UserException(UserErrorCode.INVALID_CATEGORY_IDS);
		}

		List<Integer> validCategoryIds = allCategories.stream()
			.map(CategoryClientResponseDto::getId)
			.toList();

		boolean allValid = new HashSet<>(validCategoryIds).containsAll(categoryIds);
		if (!allValid) {
			log.info("유효하지 않은 카테고리 ID 포함: categoryIds={}", categoryIds);
			throw new UserException(UserErrorCode.INVALID_CATEGORY_IDS);
		}

		// 3. 카테고리 업데이트
		user.updateCategories(categoryIds);

		// 4. 영속성 컨텍스트 플러시
		entityManager.flush();

		log.info("관심 카테고리 수정 완료: userId={}, 이전={}, 변경후={}",
			userId, oldCategoryIds, categoryIds);

		return user;
	}

	@Override
	@Transactional
	public void updatePassword(Long userId, UserPasswordUpdateRequestDto request) {
		log.info("비밀번호 수정: userId={}", userId);

		User user = validateAndGetUser(userId);

		// 1. 현재 비밀번호 확인
		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			log.info("현재 비밀번호 불일치: userId={}", userId);
			throw new UserException(UserErrorCode.PASSWORD_MISMATCH);
		}

		// 2. 새 비밀번호 확인
		if (!request.newPassword().equals(request.confirmPassword())) {
			log.info("새 비밀번호 확인 불일치: userId={}", userId);
			throw new UserException(UserErrorCode.PASSWORD_CONFIRM_MISMATCH);
		}

		// 3. 비밀번호 업데이트 (암호화)
		user.updatePassword(passwordEncoder.encode(request.newPassword()));

		log.info("비밀번호 수정 완료: userId={}", userId);
	}

	@Override
	@Transactional
	public void updateNickname(Long userId, UserNicknameUpdateRequestDto request) {
		log.info("닉네임 수정: userId={}, newNickname={}", userId, request.nickname());

		User user = validateAndGetUser(userId);
		String oldNickname = user.getNickname();

		// 중복 검사 (자기 자신 제외)
		if (userRepository.isDuplicateNickname(request.nickname(), userId)) {
			log.info("닉네임 중복: nickname={}", request.nickname());
			throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
		}

		// 닉네임 업데이트
		user.updateNickname(request.nickname());

		log.info("닉네임 수정 완료: userId={}, 이전={}, 변경후={}",
			userId, oldNickname, request.nickname());
	}

	@Override
	@Transactional
	public UserLocationResponseDto updateUserLocation(Long userId, UserLocationUpdateRequestDto request) {
		log.info("위치 정보 수정: userId={}, latitude={}, longitude={}",
			userId, request.latitude(), request.longitude());

		User user = validateAndGetUser(userId);

		// 기존 위치 백업 (로깅용)
		Double oldLatitude = user.getLatitude();
		Double oldLongitude = user.getLongitude();

		// 위치 업데이트
		user.updateLocation(request.latitude(), request.longitude());

		log.info("위치 정보 수정 완료: userId={}, 이전=({},{}), 변경후=({},{})",
			userId, oldLatitude, oldLongitude, request.latitude(), request.longitude());

		return new UserLocationResponseDto(user);
	}

	// ==================== 4. 팔로우 시스템 ====================

	@Override
	@Transactional
	public void followUser(Long followerId, Long followingId) {
		log.info("팔로우 요청: followerId={}, followingId={}", followerId, followingId);

		// 1. 자기 자신 팔로우 방지
		if (followerId.equals(followingId)) {
			log.info("자기 자신 팔로우 시도: userId={}", followerId);
			throw new UserException(UserErrorCode.CANNOT_FOLLOW_SELF);
		}

		// 2. 사용자 존재 확인
		User follower = validateAndGetUser(followerId);
		User following = validateAndGetUser(followingId);

		// 3. 중복 팔로우 확인
		boolean alreadyFollowing = follower.getFollowings().stream()
			.anyMatch(follow -> follow.getFollowingId().equals(followingId));

		if (alreadyFollowing) {
			log.info("이미 팔로우 중: followerId={}, followingId={}", followerId, followingId);
			throw new UserException(UserErrorCode.ALREADY_FOLLOWING);
		}

		// 4. 팔로우 관계 생성 및 카운트 증가
		follower.addFollowing(followingId);        // 팔로잉 추가 + 카운트 증가
		following.increaseFollowerCount();         // 팔로워 카운트만 증가

		// 5. 아웃박스 이벤트 저장
		userOutboxService.saveUserFollowedEvent(
			followerId,
			followingId,
			follower.getNickname()
		);

		// 6. 스프링 이벤트 발행
		eventPublisher.publishEvent(
			new UserEvents.Followed(
				followerId,
				followingId,
				follower.getNickname()
			)
		);

		log.info("팔로우 완료: followerId={}, followingId={}", followerId, followingId);
	}

	@Override
	@Transactional
	public void unfollowUser(Long followerId, Long followingId) {
		log.info("언팔로우 요청: followerId={}, followingId={}", followerId, followingId);

		// 1. 자기 자신 언팔로우 방지
		if (followerId.equals(followingId)) {
			log.info("자기 자신 언팔로우 시도: userId={}", followerId);
			throw new UserException(UserErrorCode.CANNOT_FOLLOW_SELF);
		}

		// 2. 사용자 존재 확인
		User follower = validateAndGetUser(followerId);
		User following = validateAndGetUser(followingId);

		// 3. 팔로우 관계 존재 확인
		boolean isFollowing = follower.getFollowings().stream()
			.anyMatch(follow -> follow.getFollowingId().equals(followingId));

		if (!isFollowing) {
			log.info("팔로우하지 않은 사용자: followerId={}, followingId={}", followerId, followingId);
			throw new UserException(UserErrorCode.NOT_FOLLOWING);
		}

		// 4. 팔로우 관계 삭제 및 카운트 감소
		follower.removeFollowing(followingId);     // 팔로잉 제거 + 카운트 감소
		following.decreaseFollowerCount();         // 팔로워 카운트만 감소

		log.info("언팔로우 완료: followerId={}, followingId={}", followerId, followingId);
	}

	@Override
	@Transactional(readOnly = true)
	public UserFollowListResponseDto getFollowings(Long userId, Pageable pageable) {
		log.debug("팔로잉 목록 조회: userId={}, page={}, size={}",
			userId, pageable.getPageNumber(), pageable.getPageSize());

		// 1. 사용자 존재 확인 및 미리 집계된 총 개수 획득
		User user = validateAndGetUser(userId);
		int totalCount = user.getFollowingCount(); // COUNT 쿼리 없이 미리 집계된 값 사용!

		// 2. 팔로잉 목록 조회 (Slice 사용으로 성능 최적화)
		Slice<User> followingsSlice = userRepository.findFollowingsByUserId(userId, pageable);

		List<UserFollowResponseDto> followingsList = followingsSlice.getContent()
			.stream()
			.map(UserFollowResponseDto::new)
			.toList();

		log.debug("팔로잉 목록 조회 완료: userId={}, 조회개수={}, 전체개수={}",
			userId, followingsList.size(), totalCount);

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
		log.debug("팔로워 목록 조회: userId={}, page={}, size={}",
			userId, pageable.getPageNumber(), pageable.getPageSize());

		// 1. 사용자 존재 확인 및 미리 집계된 총 개수 획득
		User user = validateAndGetUser(userId);
		int totalCount = user.getFollowerCount(); // COUNT 쿼리 없이 미리 집계된 값 사용!

		// 2. 팔로워 목록 조회 (Slice 사용으로 성능 최적화)
		Slice<User> followersSlice = userRepository.findFollowersByUserId(userId, pageable);

		List<UserFollowResponseDto> followersList = followersSlice.getContent()
			.stream()
			.map(UserFollowResponseDto::new)
			.toList();

		log.debug("팔로워 목록 조회 완료: userId={}, 조회개수={}, 전체개수={}",
			userId, followersList.size(), totalCount);

		return UserFollowListResponseDto.of(
			followersList,
			totalCount,
			pageable.getPageNumber(),
			pageable.getPageSize()
		);
	}

	// ==================== 5. 평가 시스템 ====================

	@Override
	@Transactional
	public void createUserRating(Long reviewerId, Long targetUserId, UserRatingCreateRequestDto request) {
		// 자기 자신 평가 방지
		if (reviewerId.equals(targetUserId)) {
			throw new UserException(UserErrorCode.CANNOT_RATE_SELF);
		}

		// 평가자 존재 확인
		validateAndGetUser(reviewerId);

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
		targetUser.addRating(reviewerId, request.meetingId(), request.ratingScore());

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

		log.info("사용자 점수 재계산 완료: userId={}, 이전점수={}, 새점수={}, " +
				"평점점수={}, 참석률점수={}, 활동도점수={}",
			userId, oldScore, totalScore, ratingScore, attendanceScore, activityScore);
	}

	/**
	 * MeetingClient를 통해 사용자의 모임 참가 통계 조회
	 * 실제 출석 정보를 활용하여 정확한 통계 계산
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
			// TODO : 모임쪽에서 N+1 해결한 메서드(배치 API)를 제공해주면 수정 예정
			ParticipantClientResponseDto userParticipant = meetingClient.getParticipant(meeting.getId(), userId);

			if (userParticipant != null && userParticipant.isAttendanceStatus()) {
				attendedMeetings++;
			}

			// 최근 3개월 참가 여부 확인 (모임 날짜 기준)
			if (meeting.getMeetingDate() != null &&
				meeting.getMeetingDate().isAfter(threeMonthsAgo)) {
				recentParticipation++;
			}
		}

		log.debug("모임 통계 계산 완료: userId={}, 총참가={}, 출석={}, 최근3개월={}",
			userId, totalParticipation, attendedMeetings, recentParticipation);

		return new UserMeetingStatsDto(totalParticipation, attendedMeetings, recentParticipation);
	}

	/**
	 * 평점 기반 점수 계산 (60%)
	 * 신규 사용자는 기본 점수 제공
	 */
	private double calculateRatingScore(User user) {
		if (user.getRatings() == null || user.getRatings().isEmpty()) {
			// 신규 사용자 기본 점수: 2.5/5.0 * 60 = 30점
			log.debug("신규 사용자 기본 평점 점수 적용: userId={}", user.getId());
			return 30.0;
		}

		// 평균 평점 계산
		double averageRating = user.getRatings().stream()
			.mapToInt(UserRating::getRatingScore)
			.average()
			.orElse(2.5); // 안전장치

		// 5점 만점을 60점 만점으로 변환
		double score = (averageRating / 5.0) * 60.0;

		log.debug("평점 점수 계산: userId={}, 평균평점={}, 점수={}",
			user.getId(), averageRating, score);

		return score;
	}

	/**
	 * 참석률 기반 점수 계산 (30%)
	 * 신청한 모임 대비 실제 참석한 모임의 비율
	 */
	private double calculateAttendanceScore(UserMeetingStatsDto meetingStats) {
		if (meetingStats.totalParticipation() == 0) {
			// 참가한 모임이 없는 경우 기본 점수: 50% 참석률 = 15점
			log.debug("모임 참가 이력 없음 - 기본 참석률 점수 적용");
			return 15.0;
		}

		// 참석률을 30점 만점으로 변환
		double attendanceRate = meetingStats.getAttendanceRate();
		double score = attendanceRate * 30.0;

		log.debug("참석률 점수 계산: 총참가={}, 출석={}, 참석률={}, 점수={}",
			meetingStats.totalParticipation(),
			meetingStats.attendedMeetings(),
			attendanceRate, score);

		return score;
	}

	/**
	 * 활동도 기반 점수 계산 (10%)
	 * 최근 3개월 모임 참가 횟수 기준
	 */
	private double calculateActivityScore(UserMeetingStatsDto meetingStats) {
		double monthlyAverage = meetingStats.getMonthlyAverage();
		double score;

		// 활동도 점수 계산
		if (monthlyAverage >= 2.0) {
			score = 10.0; // 월 평균 2회 이상 - 매우 활발
		} else if (monthlyAverage >= 1.0) {
			score = 7.0;  // 월 평균 1-2회 - 보통
		} else if (monthlyAverage > 0) {
			score = 3.0;  // 월 평균 1회 미만 - 저조
		} else {
			score = 0.0;  // 최근 활동 없음
		}

		log.debug("활동도 점수 계산: 최근3개월참가={}, 월평균={}, 점수={}",
			meetingStats.recentParticipation(), monthlyAverage, score);

		return score;
	}
}