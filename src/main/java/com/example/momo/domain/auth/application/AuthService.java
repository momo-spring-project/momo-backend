package com.example.momo.domain.auth.application;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.auth.domain.UserSocial;
import com.example.momo.domain.auth.domain.dto.AuthUser;
import com.example.momo.domain.auth.domain.dto.LoginRequestDto;
import com.example.momo.domain.auth.domain.dto.LoginResponseDto;
import com.example.momo.domain.auth.domain.dto.RegisterRequestDto;
import com.example.momo.domain.auth.domain.dto.WithdrawRequestDto;
import com.example.momo.domain.auth.infra.UserSocialRepository;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserRepository;
import com.example.momo.domain.user.exception.UserErrorCode;
import com.example.momo.domain.user.exception.UserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final UserSocialRepository userSocialRepository;

	@Transactional
	public void registerUser(RegisterRequestDto request) {

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

	public LoginResponseDto loginUser(LoginRequestDto request) {
		User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		//  비밀번호 검증
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
			throw new UserException(UserErrorCode.PASSWORD_MISMATCH);

		return new LoginResponseDto(user.getId(), user.getEmail(), user.getNickname());
	}

	@Transactional
	public void withdrawUser(WithdrawRequestDto request, AuthUser authUser) {

		User user = userRepository.findByIdAndIsDeletedFalse(authUser.getId())
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		//  비밀번호 검증
		if (user.getPassword() != null && !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new UserException(UserErrorCode.PASSWORD_MISMATCH);
		}

		// 유저는 soft delete
		user.delete();

		// 유저 소셜은 hard delete -> 연동이 끊기는 개념
		List<UserSocial> allUserSocial = userSocialRepository.findAllByUserId(user.getId());
		userSocialRepository.deleteAll(allUserSocial);
		user.delete();
	}

}
