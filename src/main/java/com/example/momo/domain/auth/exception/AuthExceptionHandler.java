package com.example.momo.domain.auth.exception;

import com.example.momo.global.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.momo.domain.auth")
@Slf4j
public class AuthExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException e) {
        log.error("JWT 예외", e);
        return ResponseEntity.status(e.getStatusCode()).body(ApiResponse.fail(e.getReason(), null));
    }
}
