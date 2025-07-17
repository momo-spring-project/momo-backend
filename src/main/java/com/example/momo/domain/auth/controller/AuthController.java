package com.example.momo.domain.auth.controller;

import com.example.momo.domain.auth.dto.*;
import com.example.momo.domain.auth.service.AuthService;
import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.global.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("회원가입에 성공했습니다.", null));
    }

    @GetMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        String token = jwtUtil.createToken(response.getId(), response.getEmail(), response.getNickname());
        return ResponseEntity.status(HttpStatus.OK)
                .header("Authorization",token)
                .body(ApiResponse.success("로그인에 성공했습니다.", response));
    }

    @DeleteMapping("withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @RequestBody @Valid WithdrawRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        authService.withdraw(request, authUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("회원 탈퇴에 성공했습니다.", null));
    }
}
