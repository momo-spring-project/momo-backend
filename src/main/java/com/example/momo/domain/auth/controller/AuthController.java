package com.example.momo.domain.auth.controller;

import com.example.momo.domain.auth.dto.*;
import com.example.momo.domain.auth.service.AuthService;
import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.global.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        // Access 토큰과 Refresh 토큰 발행
        String access = jwtUtil.createToken("access", response.getId(),  "ADMIN", JwtUtil.ACCESS_TOKEN_EXPIRE_TIME_MS);
        String refresh = jwtUtil.createToken("refresh", response.getId(),  "USER",  JwtUtil.REFRESH_TOKEN_EXPIRE_TIME_MS);


        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
                .header(HttpHeaders.SET_COOKIE, jwtUtil.createRefreshTokenCookie(refresh).toString()) // Refresh 토큰 쿠키 설정
                .body(ApiResponse.success("로그인에 성공했습니다.", response));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @RequestBody @Valid WithdrawRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        authService.withdraw(request, authUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("회원 탈퇴에 성공했습니다.", null));
    }

    @PostMapping("reissue")
    public ResponseEntity<ApiResponse<Void>> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractedRefreshToken(request);
        // 쿠키에 Refresh 토큰 있는지 확인
        if (refreshToken == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("Refresh 토큰이 존재하지 않습니다.",null));
        //  Refresh 토큰인지 확인
        if(!jwtUtil.getCategory(refreshToken).equals("refresh")) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("유효하지 않은 토큰입니다.",null));

        Long userId = jwtUtil.getUserId(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String accessToken = jwtUtil.createToken("access", userId, role, JwtUtil.ACCESS_TOKEN_EXPIRE_TIME_MS);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(ApiResponse.success("Access 토큰을 재발급했습니다.", null));
    }

    private String extractedRefreshToken(HttpServletRequest request) {
        if(request.getCookies() == null) return null;

        String refreshToken = null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue();
            }
        }
        return refreshToken;
    }
}
