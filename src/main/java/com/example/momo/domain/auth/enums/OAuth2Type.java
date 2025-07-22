package com.example.momo.domain.auth.enums;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public enum OAuth2Type {
	NAVER,
	GOOGLE;

	public static OAuth2Type fromName(String name) {
		try {
			return OAuth2Type.valueOf(name.toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new OAuth2AuthenticationException("옳지 않은 Provider명 입니다.");
		}
	}
}
