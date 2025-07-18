package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserCategory;
import com.example.momo.domain.user.domain.dto.UserEmailUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserInfoResponseDto;
import com.example.momo.domain.user.domain.dto.UserNicknameUpdateRequestDto;
import com.example.momo.domain.user.domain.dto.UserPasswordUpdateRequestDto;
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
		User user = userRepository.findById(userId)
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
		return userRepository.findById(userId)
			.orElseThrow(UserException::userNotFound);
	}

	@Override
	@Transactional
	public User updateUserCategories(Long userId, List<Integer> categoryIds) {
		try {
			User user = validateAndGetUser(userId);

			// 서비스에서 직접 카테고리 업데이트 처리
			user.getCategories().clear();
			categoryIds.forEach(categoryId ->
				user.getCategories().add(new UserCategory(categoryId))
			);

			// JPA 더티체킹으로 자동 업데이트
			return user;
		} catch (UserException e) {
			// UserException은 그대로 전파
			throw e;
		} catch (Exception e) {
			// 기타 예외(DB 제약조건 위반 등)는 카테고리 관련 예외로 변환
			throw UserException.invalidCategoryIds();
		}
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
}
