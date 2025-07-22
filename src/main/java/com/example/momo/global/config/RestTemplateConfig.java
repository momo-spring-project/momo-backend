package com.example.momo.global.config;

import com.example.momo.domain.payments.infra.toss.TossPaymentsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RestTemplateConfig {

  private final TossPaymentsConfig tossPaymentsConfig;

  @Bean
  public RestTemplate tossRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();

    // 요청 헤더에 인증 정보 및 Content-Type 설정
    ClientHttpRequestInterceptor authInterceptor = (request, body, ex) -> {
      request.getHeaders()
          .setBasicAuth(tossPaymentsConfig.getSecretKey(), ""); // Basic Auth로 secretKey 전달
      request.getHeaders().setContentType(MediaType.APPLICATION_JSON);  // JSON 요청으로 명시
      return ex.execute(request, body);
    };

    restTemplate.getInterceptors().add(authInterceptor);
    return restTemplate;
  }
}