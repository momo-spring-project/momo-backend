package com.example.momo.domain.notification.enums;

public enum UuidStatus {
	SUCCESS,      // 저장 성공
	SKIP,      // 중복 혹은 NULL
	SAVE_FAIL,    // 저장 실패 -> 재시도 대상
}
