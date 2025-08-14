package com.example.momo.global.config;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.momo.domain.payment.infra.toss.TossPaymentsConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TossWebClientConfig {

	private final TossPaymentsConfig tossPaymentsConfig;

	@Bean
	public WebClient tossWebClient(
		@Qualifier("externalWebClient") WebClient externalWebClient) {

		// 공용 externalWebClient 기반
		return externalWebClient.mutate()
			.baseUrl(tossPaymentsConfig.getBaseUrl())
			.defaultHeaders(h -> h.setBasicAuth(
				tossPaymentsConfig.getSecretKey(), "", StandardCharsets.UTF_8))
			.filters(fs -> {
				fs.add(tossLogFilter());
			})
			.build();
	}

	/** TOSS 전용 로그 필터 */
	private ExchangeFilterFunction tossLogFilter() {
		return ExchangeFilterFunction.ofRequestProcessor(req -> {
			log.debug("[TOSS] {} {}", req.method(), req.url());
			return Mono.just(req);
		}).andThen(ExchangeFilterFunction.ofResponseProcessor(res -> {
			if (res.statusCode().isError()) {
				log.error("[TOSS] {} {}", res.statusCode(), res.request().getURI());
			} else {
				log.debug("[TOSS] {} {}", res.statusCode(), res.request().getURI());
			}
			return Mono.just(res);
		}));
	}
}
