package com.example.momo.domain.auth.controller;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.domain.auth.dto.RegisterRequest;
import com.example.momo.domain.auth.service.AuthService;
import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.global.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("회원가입에 성공했습니다.", null));
    }

}
