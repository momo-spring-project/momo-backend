package com.example.momo.domain.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.dto.UserInfoResponseDto;
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
}
