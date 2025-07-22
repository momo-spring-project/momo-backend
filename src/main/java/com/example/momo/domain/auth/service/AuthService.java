package com.example.momo.domain.auth.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.domain.auth.dto.LoginRequest;
import com.example.momo.domain.auth.dto.LoginResponse;
import com.example.momo.domain.auth.dto.RegisterRequest;
import com.example.momo.domain.auth.dto.WithdrawRequest;
import com.example.momo.domain.auth.entity.UserSocial;
import com.example.momo.domain.auth.repository.UserSocialRepository;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.exception.UserErrorCode;
import com.example.momo.domain.user.exception.UserException;
import com.example.momo.domain.user.infra.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final UserSocialRepository userSocialRepository;

	@Transactional
	public void register(RegisterRequest request) {

		if (userRepository.existsByNickname(request.getNickname())) {
			throw new UserException(UserErrorCode.USER_NOT_FOUND);
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
		}
		User user = new User(
			request.getNickname(),
			request.getEmail(),
			passwordEncoder.encode(request.getPassword()),
			null,
			request.getLatitude(),
			request.getLongitude()
		);
		userRepository.save(user);
	}

	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		//  비밀번호 검증
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
			throw new UserException(UserErrorCode.PASSWORD_MISMATCH);

		return new LoginResponse(user.getId(), user.getEmail(), user.getNickname());
	}

	@Transactional
	public void withdraw(WithdrawRequest request, AuthUser authUser) {

		User user = userRepository.findByIdAndIsDeletedFalse(authUser.getId())
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		//  비밀번호 검증
		if (user.getPassword() != null && !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new UserException(UserErrorCode.PASSWORD_MISMATCH);
		}

		// 유저는 soft delete
		user.delete();

		// 유저 소셜은 hard delete -> 연동이 끊기는 개념
		List<UserSocial> allUserSocial = userSocialRepository.findAllByUser(user);
		userSocialRepository.deleteAll(allUserSocial);
		user.delete();
	}

}
