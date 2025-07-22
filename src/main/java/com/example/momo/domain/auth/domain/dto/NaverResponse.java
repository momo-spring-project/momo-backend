package com.example.momo.domain.auth.domain.dto;

import java.util.Map;
import java.util.UUID;

public class NaverResponse implements OAuth2Response {
	private final Map<String, Object> attribute;

	public NaverResponse(Map<String, Object> attribute) {

		this.attribute = (Map<String, Object>)attribute.get("response");
	}

	@Override
	public String getProvider() {
		return "naver";
	}

	@Override
	public String getProviderId() {
		return attribute.get("id").toString();
	}

	@Override
	public String getEmail() {
		return attribute.get("email").toString();
	}

	@Override
	public String getNickname() {
		return "NAVER:" + UUID.randomUUID().toString().substring(0, 18);
	}

	@Override
	public Map<String, Object> getAttribute() {
		return attribute;
	}
}
