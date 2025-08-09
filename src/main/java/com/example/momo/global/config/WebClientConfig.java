package com.example.momo.global.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

/**
 * WebClient 설정 클래스
 * 프로덕션 레벨의 HTTP 클라이언트 설정 포함
 * - 커넥션 풀 관리
 * - 재시도 및 회복력 패턴
 * - 로깅 및 모니터링
 * - 보안 헤더 처리
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

	private final BCryptPasswordEncoder passwordEncoder;

	@Value("${webclient.internal.secret-key}")
	private String webSecretKey;

	@Value("${webclient.base-url:http://localhost:8080}")
	private String baseUrl;

	/**
	 * 내부 서비스 통신용 WebClient
	 * 재시도, 로깅, 커넥션 풀 관리 포함
	 */
	@Bean("internalWebClient")
	public WebClient internalWebClient() {
		return WebClient.builder()
			.baseUrl(baseUrl)
			.clientConnector(new ReactorClientHttpConnector(createHttpClient()))
			.exchangeStrategies(createExchangeStrategies())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader("WebclientInternal", passwordEncoder.encode(webSecretKey))
			.defaultHeader("User-Agent", "Momo-Internal-Client/1.0")
			.filter(loggingFilter())
			.filter(retryFilter())
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
			.build();
	}

	/**
	 * 외부 API 통신용 WebClient
	 * 외부 서비스(결제, SMS 등) 호출용
	 */
	@Bean("externalWebClient")
	public WebClient externalWebClient() {
		return WebClient.builder()
			.clientConnector(new ReactorClientHttpConnector(createExternalHttpClient()))
			.exchangeStrategies(createExchangeStrategies())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader("User-Agent", "Momo-External-Client/1.0")
			.filter(loggingFilter())
			.filter(externalRetryFilter())
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)) // 5MB
			.build();
	}

	/**
	 * 내부 서비스용 HTTP 클라이언트 설정
	 * 빠른 응답과 높은 처리량 최적화
	 */
	private HttpClient createHttpClient() {
		ConnectionProvider connectionProvider = ConnectionProvider.builder("internal-pool")
			.maxConnections(50)                    // 최대 커넥션 수
			.maxIdleTime(Duration.ofSeconds(30))   // 유휴 시간
			.maxLifeTime(Duration.ofMinutes(10))   // 최대 생존 시간
			.pendingAcquireTimeout(Duration.ofSeconds(10)) // 커넥션 대기 시간
			.evictInBackground(Duration.ofSeconds(60))     // 백그라운드 정리 주기
			.build();

		return HttpClient.create(connectionProvider)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)  // 연결 타임아웃 3초
			.responseTimeout(Duration.ofSeconds(10))              // 응답 타임아웃 10초
			.doOnConnected(conn ->
				conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
					.addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)))
			.compress(true)  // gzip 압축 활성화
			.keepAlive(true) // Keep-Alive 활성화
			.wiretap(false); // 개발환경에서만 true로 설정
	}

	/**
	 * 외부 API용 HTTP 클라이언트 설정
	 * 안정성과 재시도에 중점
	 */
	private HttpClient createExternalHttpClient() {
		ConnectionProvider connectionProvider = ConnectionProvider.builder("external-pool")
			.maxConnections(20)                    // 외부 API는 더 적은 커넥션
			.maxIdleTime(Duration.ofSeconds(20))
			.maxLifeTime(Duration.ofMinutes(5))
			.pendingAcquireTimeout(Duration.ofSeconds(15))
			.evictInBackground(Duration.ofSeconds(30))
			.build();

		return HttpClient.create(connectionProvider)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)   // 외부 API는 더 긴 타임아웃
			.responseTimeout(Duration.ofSeconds(30))
			.doOnConnected(conn ->
				conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
					.addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)))
			.compress(true)
			.keepAlive(true)
			.wiretap(false);
	}

	/**
	 * 메모리 버퍼 크기 설정
	 */
	private ExchangeStrategies createExchangeStrategies() {
		return ExchangeStrategies.builder()
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB
			.build();
	}

	/**
	 * 요청/응답 로깅 필터
	 * 개발환경에서는 상세 로그, 운영환경에서는 에러만
	 */
	private ExchangeFilterFunction loggingFilter() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			log.debug("WebClient Request: {} {}", clientRequest.method(), clientRequest.url());
			return Mono.just(clientRequest);
		}).andThen(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			if (clientResponse.statusCode().isError()) {
				log.error("WebClient Error Response: {} {}",
					clientResponse.statusCode(), clientResponse.request().getURI());
			} else {
				log.debug("WebClient Response: {} {}",
					clientResponse.statusCode(), clientResponse.request().getURI());
			}
			return Mono.just(clientResponse);
		}));
	}

	/**
	 * 내부 서비스용 재시도 필터
	 * 빠른 실패와 복구
	 */
	private ExchangeFilterFunction retryFilter() {
		return (request, next) -> next.exchange(request)
			.retryWhen(Retry.backoff(3, Duration.ofMillis(500))
				.maxBackoff(Duration.ofSeconds(2))
				.filter(throwable -> {
					// 5xx 에러와 커넥션 에러만 재시도
					if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
						var ex = (org.springframework.web.reactive.function.client.WebClientResponseException)throwable;
						return ex.getStatusCode().is5xxServerError();
					}
					return throwable instanceof java.net.ConnectException ||
						throwable instanceof java.util.concurrent.TimeoutException;
				})
				.doBeforeRetry(retrySignal ->
					log.warn("Retrying request to {}, attempt: {}",
						request.url(), retrySignal.totalRetries() + 1)));
	}

	/**
	 * 외부 API용 재시도 필터
	 * 더 관대한 재시도 정책
	 */
	private ExchangeFilterFunction externalRetryFilter() {
		return (request, next) -> next.exchange(request)
			.retryWhen(Retry.backoff(5, Duration.ofSeconds(1))
				.maxBackoff(Duration.ofSeconds(10))
				.filter(throwable -> {
					if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
						var ex = (org.springframework.web.reactive.function.client.WebClientResponseException)throwable;
						// 4xx 에러는 재시도하지 않음 (클라이언트 에러)
						return ex.getStatusCode().is5xxServerError() ||
							ex.getStatusCode().value() == 429; // Rate Limit
					}
					return throwable instanceof java.net.ConnectException ||
						throwable instanceof java.util.concurrent.TimeoutException;
				})
				.doBeforeRetry(retrySignal ->
					log.warn("Retrying external API request to {}, attempt: {}",
						request.url(), retrySignal.totalRetries() + 1)));
	}
}