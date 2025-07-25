package com.example.momo.global.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient 설정 클래스
 * 도메인간 통신을 위한 WebClient 빈을 생성
 */
@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient() {
		// HTTP 클라이언트 커넥션 풀 및 타임아웃 설정
		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 연결 타임아웃 5초
			.responseTimeout(Duration.ofSeconds(5)) // 응답 타임아웃 5초
			.doOnConnected(conn ->
				conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS)) // 읽기 타임아웃 5초
					.addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))); // 쓰기 타임아웃 5초

		return WebClient.builder()
			.baseUrl("http://localhost:8080") // 기본 URL 설정
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.filter(authorizationHeaderFilter())
			.build();
	}

	private ExchangeFilterFunction authorizationHeaderFilter() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			if (clientRequest.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
				return Mono.just(clientRequest);
			}
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if(authentication != null && authentication.isAuthenticated()) {
				Object credentials = authentication.getCredentials();
				if(credentials instanceof String token) {
					ClientRequest newRequest = ClientRequest.from(clientRequest)
						.headers(headers -> headers.setBearerAuth(token))
						.build();
					return Mono.just(newRequest);
				}
			}
			return Mono.just(clientRequest);
		});
	}
}