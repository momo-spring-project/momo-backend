package com.example.momo.domain.auth.application;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.auth.domain.dto.LoginRequestDto;
import com.example.momo.domain.auth.domain.dto.LoginResponseDto;
import com.example.momo.domain.user.domain.dto.UserAuthResponseDto;
import com.example.momo.domain.user.exception.UserErrorCode;
import com.example.momo.domain.user.exception.UserException;
import com.example.momo.global.infrastructure.client.user.UserClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserClient userClient;
	private final BCryptPasswordEncoder passwordEncoder;

	// 로그인만 Auth 도메인에서 담당
	public LoginResponseDto loginUser(LoginRequestDto request, String internalToken) {

		UserAuthResponseDto user = userClient.getUserByEmailForAuth(request.getEmail(), internalToken);

		if (user == null) {
			throw new UserException(UserErrorCode.USER_NOT_FOUND);
		}

		// 비밀번호 검증 (소셜 로그인 사용자는 password가 null일 수 있음)
		if (user.password() != null && !passwordEncoder.matches(request.getPassword(), user.password())) {
			throw new UserException(UserErrorCode.PASSWORD_MISMATCH);
		}

		return new LoginResponseDto(user.id(), user.email(), user.nickname());
	}
}
