package com.example.momo.domain.notification.enums;

/*
 * FCM 전송 후 상태 메세지 반환
 * 성공, 일시 실패, 토큰 삭제로 구분
 * 일시 실패는 토큰은 유효하나 전송 실패
 * 토큰 삭제는 토큰 유효성 실패
 */
public enum SendStatus {
	SUCCESS,
	FAILURE,
	DELETE_TOKEN
}
