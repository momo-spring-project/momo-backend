package com.example.momo.domain.user.api;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.domain.user.application.UserService;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.dto.UserCategoryUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserCategoryUpdateResponseDto;
import com.example.momo.domain.user.domain.dto.UserEmailUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserFollowInfoResponseDto;
import com.example.momo.domain.user.domain.dto.UserInfoResponseDto;
import com.example.momo.domain.user.domain.dto.UserNicknameUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserPasswordUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserRatingCreateRequestDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	// 특정 사용자 정보 조회
	@GetMapping("{userId}")
	public ResponseEntity<ApiResponse<UserInfoResponseDto>> getUserInfo(
		@PathVariable Long userId
	) {
		UserInfoResponseDto response = userService.getUserById(userId);
		return ResponseEntity.ok(ApiResponse.success("사용자 정보를 조회했습니다.", response));
	}

	// 현재 로그인안 사용자 정보 조회 (추후에 수정 예정)
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserInfoResponseDto>> getCurrentUser(
		@RequestHeader("user_id") Long currentUserId // TODO : 임시로 설정
	) {
		UserInfoResponseDto response = userService.getCurrentUser(currentUserId);
		return ResponseEntity.ok(ApiResponse.success("내 정보를 조회했습니다.", response));
	}

	// 내 관심 카테고리 수정
	@PatchMapping("/me/categories")
	public ResponseEntity<ApiResponse<UserCategoryUpdateResponseDto>> updateMyCategories(
		@RequestHeader("user_id") Long currentUserId, // TODO : 임시로 설정
		@Valid @RequestBody UserCategoryUpdateRequestDto request
	) {
		User user = userService.updateUserCategories(currentUserId, request.categoryIds());
		UserCategoryUpdateResponseDto response = new UserCategoryUpdateResponseDto(user);
		return ResponseEntity.ok(ApiResponse.success("내 관심 카테고리가 수정되었습니다.", response));
	}

	// 내 비밀번호 수정
	@PatchMapping("/me/password")
	public ResponseEntity<ApiResponse<Void>> updateMyPassword(
		@RequestHeader("user_id") Long currentUserId, // TODO : 임시로 설정
		@Valid @RequestBody UserPasswordUpdateRequestDto request
	) {
		userService.updatePassword(currentUserId, request);
		return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
	}

	// 내 닉네임 수정
	@PatchMapping("/me/nickname")
	public ResponseEntity<ApiResponse<Void>> updateMyNickname(
		@RequestHeader("user_id") Long currentUserId, // TODO : 임시로 설정
		@Valid @RequestBody UserNicknameUpdateRequestDto request
	) {
		userService.updateNickname(currentUserId, request);
		return ResponseEntity.ok(ApiResponse.success("닉네임이 변경되었습니다.", null));
	}

	// 내 이메일 수정
	@PatchMapping("/me/email")
	public ResponseEntity<ApiResponse<Void>> updateMyEmail(
		@RequestHeader("user_id") Long currentUserId, // TODO : 임시로 설정
		@Valid @RequestBody UserEmailUpdateRequestDto request
	) {
		userService.updateEmail(currentUserId, request);
		return ResponseEntity.ok(ApiResponse.success("이메일이 변경되었습니다.", null));
	}

	// 특정 사용자 평가하기
	@PostMapping("/{targetUserId}/ratings")
	public ResponseEntity<ApiResponse<Void>> createUserRating(
		@RequestHeader("user_id") Long reviewerId, // TODO : 임시로 설정
		@PathVariable Long targetUserId,
		@Valid @RequestBody UserRatingCreateRequestDto request
	) {
		userService.createUserRating(reviewerId, targetUserId, request);

		return ResponseEntity.ok(ApiResponse.success("사용자 평가가 등록되었습니다.", null));
	}

	// 사용자 팔로우
	@PostMapping("/{followingId}/followings")
	public ResponseEntity<ApiResponse<Void>> followUser(
		@RequestHeader("user_id") Long followerId, // TODO : 임시로 설정
		@PathVariable Long followingId
	) {
		userService.followUser(followerId, followingId);
		return ResponseEntity.ok(ApiResponse.success("팔로우가 완료되었습니다.", null));
	}

	// 사용자 언팔로우
	@DeleteMapping("/{followingId}/followings")
	public ResponseEntity<ApiResponse<Void>> unfollowUser(
		@RequestHeader("user_id") Long followerId, // TODO : 임시로 설정
		@PathVariable Long followingId
	) {
		userService.unfollowUser(followerId, followingId);
		return ResponseEntity.ok(ApiResponse.success("언팔로우가 완료되었습니다.", null));
	}

	// 특정 사용자의 팔로잉 목록 조회
	@GetMapping("/{userId}/followings")
	public ResponseEntity<ApiResponse<List<UserFollowInfoResponseDto>>> getUserFollowings(
		@PathVariable Long userId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		List<UserFollowInfoResponseDto> response = userService.getFollowings(userId, pageable);
		return ResponseEntity.ok(ApiResponse.success("팔로잉 목록을 조회했습니다.", response));
	}

	// 특정 사용자의 팔로워 목록 조회
	@GetMapping("/{userId}/followers")
	public ResponseEntity<ApiResponse<List<UserFollowInfoResponseDto>>> getUserFollowers(
		@PathVariable Long userId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		List<UserFollowInfoResponseDto> response = userService.getFollowers(userId, pageable);
		return ResponseEntity.ok(ApiResponse.success("팔로워 목록을 조회했습니다.", response));
	}

	// 내 팔로잉 목록 조회
	@GetMapping("/me/followings")
	public ResponseEntity<ApiResponse<List<UserFollowInfoResponseDto>>> getMyFollowings(
		@RequestHeader("user_id") Long currentUserId, // TODO : 임시로 설정
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		List<UserFollowInfoResponseDto> response = userService.getFollowings(currentUserId, pageable);
		return ResponseEntity.ok(ApiResponse.success("내 팔로잉 목록을 조회했습니다.", response));
	}

	// 내 팔로워 목록 조회
	@GetMapping("/me/followers")
	public ResponseEntity<ApiResponse<List<UserFollowInfoResponseDto>>> getMyFollowers(
		@RequestHeader("user_id") Long currentUserId, // TODO : 임시로 설정
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		List<UserFollowInfoResponseDto> response = userService.getFollowers(currentUserId, pageable);
		return ResponseEntity.ok(ApiResponse.success("내 팔로워 목록을 조회했습니다.", response));
	}
}
