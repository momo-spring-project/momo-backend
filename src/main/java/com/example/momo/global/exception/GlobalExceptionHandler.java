package com.example.momo.global.exception;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.momo.domain.common.dto.ApiResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	private final MessageSource messageSource;

	// 비즈니스 로직에서 발생한 커스텀 예외 처리
	@ExceptionHandler(BaseException.class)
	protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BaseException e) {
		return ResponseEntity
			.status(e.getErrorCode().getStatus()) // 예외에 정의된 상태 코드로 응답
			.body(ApiResponse.fail(e.getMessage(), null)); // 공통 응답 포맷으로 메시지 반환
	}

	// @Valid로 DTO를 검증할 때 발생하는 예외 처리 (RequestBody 검증 실패)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ApiResponse<List<String>>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {

		// 검증 실패한 메시지를 모두 모아서 리스트로 반환
		List<String> errors = e.getBindingResult()
			.getAllErrors()
			.stream()
			.map(objectError -> messageSource.getMessage(objectError, Locale.KOREAN))
			.toList();

		return ResponseEntity
			.badRequest() // 400 Bad Request
			.body(ApiResponse.fail(String.join("||", errors), null)); // || 구분자로 묶어 반환
	}

	// null 값을 입력하였을 경우
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleJsonParseError(HttpMessageNotReadableException e) {
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.fail("입력 값을 다시 확인해주세요.", null));
	}

	//@RequestParam 또는 @PathVariable 등의 필수 값이 빠졌을 때
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
		return ResponseEntity
			.badRequest()
			.body(ApiResponse.fail("요청 파라미터가 누락되었습니다: " + e.getParameterName(), null));
	}

	//@Validated + @RequestParam 검증 실패 시 발생
	@ExceptionHandler(ConstraintViolationException.class)
	protected ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
		String errorMessage = e.getConstraintViolations()
			.stream()
			.map(ConstraintViolation::getMessage)
			.collect(Collectors.joining("||"));
		return ResponseEntity
			.badRequest()
			.body(ApiResponse.fail(errorMessage, null));
	}

	//지원하지 않는 HTTP 메서드 (예: POST 만 지원하는데 GET 요청)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return ResponseEntity
			.status(HttpStatus.METHOD_NOT_ALLOWED)
			.body(ApiResponse.fail("지원하지 않는 HTTP 메서드입니다.", null));
	}

	// 예상하지 못한 런타임 예외 처리
	@ExceptionHandler(RuntimeException.class)
	protected ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.fail("예상치 못한 오류가 발생했습니다.", null));
	}

	// 그 외 모든 예외 처리 (최후의 보루)
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.fail("서버 내부 오류가 발생했습니다.", null));
	}
}
