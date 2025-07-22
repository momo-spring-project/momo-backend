package com.example.momo.domain.payment.infra.toss;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

  private final RestTemplate tossRestTemplate;
  private final TossPaymentsConfig config;

  // ==================== 결제 처리 API ====================

  public Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount) {
    String url = config.getBaseUrl() + "/payments/confirm";

    // RestTemplate 인터셉터에서 이미 인증 헤더를 설정하므로 여기서는 Idempotency-Key만 추가
    HttpHeaders headers = new HttpHeaders();
    addIdempotencyKey(headers, "confirm-" + orderId);

    Map<String, Object> body = Map.of(
        "paymentKey", paymentKey,
        "orderId", orderId,
        "amount", amount
    );

    HttpEntity<?> entity = new HttpEntity<>(body, headers);
    Map<String, Object> res = tossRestTemplate
        .exchange(url, HttpMethod.POST, entity, Map.class)
        .getBody();

    log.info("[TOSS] 결제 승인 완료 - orderId: {}, amount: {}", orderId, amount);
    return res;
  }

  public Map<String, Object> cancelPayment(String paymentKey, String reason) {
    String url = config.getBaseUrl() + "/payments/{paymentKey}/cancel";

    HttpHeaders headers = new HttpHeaders();
    addIdempotencyKey(headers, "cancel-" + paymentKey);

    Map<String, Object> body = Map.of("cancelReason", reason);
    HttpEntity<?> entity = new HttpEntity<>(body, headers);

    Map<String, Object> res = tossRestTemplate
        .exchange(url, HttpMethod.POST, entity, Map.class, paymentKey)
        .getBody();

    log.info("[TOSS] 결제 취소 완료 - paymentKey: {}, reason: {}", paymentKey, reason);
    return res;
  }

  // ==================== 조회 API ====================

  public Map<String, Object> getPayment(String paymentKey) {
    String url = config.getBaseUrl() + "/payments/{paymentKey}";
    Map<String, Object> result = tossRestTemplate.getForObject(url, Map.class, paymentKey);

    log.debug("[TOSS] 결제 조회 - paymentKey: {}", paymentKey);
    return result;
  }

  // ==================== 테스트 전용 API ====================

  public Map<String, Object> createTestKeyInPayment(Map<String, Object> payload, String orderId) {
    String url = config.getBaseUrl() + "/payments/key-in";  // 토스 API 문서에 따른 엔드포인트

    HttpHeaders headers = new HttpHeaders();
    addIdempotencyKey(headers, "keyin-" + orderId); // UUID 기반으로 고정된 키 사용

    HttpEntity<?> entity = new HttpEntity<>(payload, headers);

    try {
      Map<String, Object> res = tossRestTemplate
          .exchange(url, HttpMethod.POST, entity, Map.class)
          .getBody();

      log.info("[TOSS] Key-in 결제 생성 완료 - orderId: {}", orderId);
      return res;
    } catch (Exception e) {
      log.error("[TOSS] Key-in 결제 실패 - orderId: {}, error: {}", orderId, e.getMessage());
      throw e;
    }
  }

  // ==================== Private Helper 메서드 ====================

  /**
   * Idempotency Key 추가 토스페이먼츠는 15일 동안 동일 키 보관
   */
  private void addIdempotencyKey(HttpHeaders headers, String keySeed) {
    headers.add("Idempotency-Key", keySeed);
  }
}