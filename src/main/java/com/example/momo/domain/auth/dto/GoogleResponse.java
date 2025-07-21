package com.example.momo.domain.auth.dto;

import java.util.Map;
import java.util.UUID;

public class GoogleResponse implements OAuth2Response {
	private final Map<String, Object> attribute;

	public GoogleResponse(Map<String, Object> attribute) {
		this.attribute = attribute;
	}

	@Override
	public String getProvider() {
		return "google";
	}

	@Override
	public String getProviderId() {
		return attribute.get("sub").toString();
	}

	@Override
	public String getEmail() {
		return attribute.get("email").toString();
	}

	@Override
	public String getNickname() {
		return "GOOGLE:" + UUID.randomUUID().toString().substring(0, 18);
	}

	@Override
	public Map<String, Object> getAttribute() {
		return attribute;
	}
}
