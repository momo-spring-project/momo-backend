package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserFollow;
import com.example.momo.domain.user.domain.UserRating;
import com.example.momo.domain.user.domain.dto.UserEmailUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserFollowInfoResponseDto;
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

	@Override
	@Transactional
	public User updateUserCategories(Long userId, List<Integer> categoryIds) {
		User user = validateAndGetUser(userId);

		// TODO: 카테고리 존재 여부 검증 (CategoryService 연동 후 구현)
		// List<Category> categories = categoryService.getCategories(categoryIds);
		// if (categories.size() != categoryIds.size()) {
		//     throw UserException.invalidCategoryIds();
		// }

		user.updateCategories(categoryIds);
		return user;
	}

	@Override
	@Transactional
	public void updatePassword(Long userId, UserPasswordUpdateRequestDto request) {
		User user = validateAndGetUser(userId);

		// 서비스에서 현재 비밀번호 확인 (TODO: 암호화된 비밀번호와 비교)
		if (!user.getPassword().equals(request.currentPassword())) {
			throw UserException.passwordMismatch();
		}

		if (!request.newPassword().equals(request.confirmPassword())) {
			throw UserException.passwordConfirmMismatch();
		}

		user.updatePassword(request.newPassword());
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

		// 서비스에서 현재 비밀번호 확인 (TODO: 암호화된 비밀번호와 비교)
		if (!user.getPassword().equals(request.currentPassword())) {
			throw UserException.passwordMismatch();
		}

		if (userRepository.existsByEmailAndIdNot(request.email(), userId)) {
			throw UserException.duplicateEmail();
		}

		user.updateEmail(request.email());
	}

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

		// 4. 같은 모임 참가 여부 확인
		// TODO: Meeting 도메인과 연동하여 실제 검증 로직 구현
		validateSameMeetingParticipants(reviewerId, targetUserId, request.meetingId());

		// 5. 중복 평가 확인 - targetUser의 ratings에서 확인
		boolean alreadyRated = targetUser.getRatings().stream()
			.anyMatch(rating ->
				rating.getReviewerId().equals(reviewerId) &&
					rating.getMeetingId().equals(request.meetingId())
			);

		if (alreadyRated) {
			throw UserException.duplicateRating();
		}

		// 6. 평가 생성 및 targetUser의 ratings에 추가
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
		// TODO: Meeting 도메인과 연동하여 실제 검증 로직 구현
		// 현재는 임시로 모든 경우를 허용
		// 실제 구현시에는 MeetingParticipant 테이블을 조회하여
		// 두 사용자가 모두 해당 모임에 참가했는지 확인해야 함
	}

	@Override
	@Transactional
	public void recalculateUserScore(Long userId) {
		User user = validateAndGetUser(userId);

		// 1. 평점 점수 계산 (60%)
		double ratingScore = calculateRatingScore(user);

		// 2. 참석률 점수 계산 (30%) - TODO: Meeting 도메인 연동 후 구현
		double attendanceScore = calculateAttendanceScore(user);

		// 3. 활동도 점수 계산 (10%) - TODO: Meeting 도메인 연동 후 구현
		double activityScore = calculateActivityScore(user);

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
	 * TODO: Meeting 도메인과 연동하여 구현
	 */
	private double calculateAttendanceScore(User user) {
		// 임시로 기본 점수 반환
		// 실제 구현시에는 모임 신청 수 대비 실제 참석 수 계산
		return 21.0; // 70% 참석률 가정
	}

	/**
	 * 활동도 기반 점수 계산 (10%)
	 * TODO: Meeting 도메인과 연동하여 구현
	 */
	private double calculateActivityScore(User user) {
		// 임시로 기본 점수 반환
		// 실제 구현시에는 최근 3개월 모임 참가 횟수 계산
		return 7.0; // 월 평균 1-2회 활동 가정
	}

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
	public List<UserFollowInfoResponseDto> getFollowings(Long userId, Pageable pageable) {
		// 1. 사용자 존재 확인
		validateAndGetUser(userId);

		// 2. 팔로잉 목록 조회
		List<User> followings = userRepository.findFollowingsByUserId(userId, pageable);

		// 3. DTO 변환
		return followings.stream()
			.map(UserFollowInfoResponseDto::new)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserFollowInfoResponseDto> getFollowers(Long userId, Pageable pageable) {
		// 1. 사용자 존재 확인
		validateAndGetUser(userId);

		// 2. 팔로워 목록 조회
		List<User> followers = userRepository.findFollowersByUserId(userId, pageable);

		// 3. DTO 변환
		return followers.stream()
			.map(UserFollowInfoResponseDto::new)
			.toList();
	}
}
