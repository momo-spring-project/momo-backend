package com.example.momo.domain.auth.service;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.domain.auth.dto.RegisterRequest;
import com.example.momo.domain.users.entity.User;
import com.example.momo.domain.users.infra.UserRepository;
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
            // TODO : 커스텀 예외로 변경
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            // TODO : 커스텀 예외로 변경
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

}
