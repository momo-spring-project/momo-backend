package com.example.momo.domain.auth.service;

import com.example.momo.domain.auth.dto.*;
import com.example.momo.domain.users.entity.User;
import com.example.momo.domain.users.infra.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
        // TODO : 커스텀 예외로 변경
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
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
        // TODO : 커스텀 예외로 변경
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail()).orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        //  비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        return new LoginResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    @Transactional
    public void withdraw(WithdrawRequest request, AuthUser authUser) {
        // TODO : 커스텀 예외로 변경
        User user = userRepository.findByIdAndIsDeletedFalse(authUser.getId()).orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        //  비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        user.delete();
    }

}
