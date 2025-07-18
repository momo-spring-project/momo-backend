package com.example.momo.domain.auth.service;

import com.example.momo.domain.auth.dto.*;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.exception.UserException;
import com.example.momo.domain.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByNickname(request.getNickname())) {
            throw UserException.duplicateNickname();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw UserException.duplicateEmail();
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
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail()).orElseThrow(UserException::userNotFound);
        //  비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) throw UserException.passwordMismatch();

        return new LoginResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Transactional
    public void withdraw(WithdrawRequest request, AuthUser authUser) {
        User user = userRepository.findByIdAndIsDeletedFalse(authUser.getId()).orElseThrow(UserException::userNotFound);
        //  비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) throw UserException.passwordMismatch();

        user.delete();
    }

}
