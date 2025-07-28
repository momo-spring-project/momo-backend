package com.example.momo.global.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.momo.domain.payment.infra.toss.TossPaymentsConfig;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TossWebClientConfig {

	private final TossPaymentsConfig tossPaymentsConfig;

	@Bean
	public WebClient tossWebClient() {
		// HTTP 클라이언트 커넥션 풀 및 타임아웃 설정
		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // TCP 커넥션을 맺을 때까지 기다리는 최대 시간
			.responseTimeout(Duration.ofSeconds(5)) //요청을 보내고 응답을 받기까지 기다리는 전체 시간
			.doOnConnected(conn ->
				conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS)) // 서버로부터 응답 본문을 읽는 데 걸리는 시간
					.addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))); // 요청 body를 서버로 쓰는(보내는) 데 걸리는 시간

		// Basic Auth 헤더 생성
		String auth = tossPaymentsConfig.getSecretKey() + ":";
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

		return WebClient.builder()
			.baseUrl(tossPaymentsConfig.getBaseUrl())
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.defaultHeader("Authorization", "Basic " + encodedAuth)
			.defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
			.filter(logRequest())
			.filter(logResponse())
			.build();
	}

	// 요청 로깅
	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			log.debug("[TOSS] Request: {} {}", clientRequest.method(), clientRequest.url());
			return Mono.just(clientRequest);
		});
	}

	// 응답 로깅
	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			log.debug("[TOSS] Response status: {}", clientResponse.statusCode());
			return Mono.just(clientResponse);
		});
	}
}