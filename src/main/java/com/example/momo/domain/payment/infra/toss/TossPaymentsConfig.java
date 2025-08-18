package com.example.momo.domain.payment.infra.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "toss.payments")
public class TossPaymentsConfig {

	private final String secretKey;
	private final String clientKey;
	private final String baseUrl;

	public TossPaymentsConfig(
		String secretKey,
		String clientKey,
		@DefaultValue("https://api.tosspayments.com/v1") String baseUrl) {
		this.secretKey = secretKey;
		this.clientKey = clientKey;
		this.baseUrl = baseUrl;

	}

	public String getBaseUrl() {
		return baseUrl != null ? baseUrl.trim() : null; // 공백 제거
	}
}
