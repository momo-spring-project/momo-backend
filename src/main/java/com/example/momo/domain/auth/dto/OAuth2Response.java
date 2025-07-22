package com.example.momo.domain.auth.dto;

import java.util.Map;

public interface OAuth2Response {
	// 제공자 (Ex. naver, google)
	String getProvider();

	// 제공자에서 발급해주는 아이디의 고유 식별자
	String getProviderId();

	// 이메일
	String getEmail();

	// 닉네임
	String getNickname();

	Map<String, Object> getAttribute();
}
