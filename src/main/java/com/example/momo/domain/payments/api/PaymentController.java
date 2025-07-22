package com.example.momo.domain.payments.api;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.domain.payments.application.PaymentService;
import com.example.momo.domain.payments.dto.CardPaymentTestRequest;
import com.example.momo.domain.payments.dto.PaymentResponse;
import com.example.momo.domain.payments.dto.RefundRequest;
import com.example.momo.domain.payments.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  // 테스트 키인 결제 (카드번호 직접 입력)
  @PostMapping("/test/keyin")
  public ResponseEntity<ApiResponse<PaymentResponse>> testKeyIn(
      @RequestBody CardPaymentTestRequest request,
      @AuthenticationPrincipal AuthUser authUser) {

    PaymentResponse response = paymentService.createTestKeyInPayment(request, authUser.getId());
    return ResponseEntity.ok(ApiResponse.success("테스트 키인 결제가 완료되었습니다.", response));
  }


  /**
   * 관리자용 다중 조건 검색 ex)
   * /search?meetingId=1&userId=3&status=COMPLETED&page=0&size=20&sort=paidAt,desc
   */
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<Page<PaymentResponse>>> searchPayments(
      @RequestParam(required = false) Long meetingId,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) PaymentStatus status,
      Pageable pageable) {

    Page<PaymentResponse> page = paymentService.searchPayments(meetingId, userId, status, pageable);
    return ResponseEntity.ok(ApiResponse.success("결제 내역 검색 완료", page));
  }

  // 환불 처리
  @PostMapping("/{paymentId}/refund")
  public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
      @PathVariable Long paymentId,
      @RequestBody RefundRequest request,
      @AuthenticationPrincipal AuthUser authUser) {
    PaymentResponse response = paymentService.refundPayment(paymentId, authUser.getId(), request);
    return ResponseEntity.ok(ApiResponse.success("환불이 완료되었습니다.", response));
  }

//------------------------------//
//
//  // 모임별 결제 내역 조회
//  @GetMapping("/meetings/{meetingId}")
//  public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByMeeting(
//      @PathVariable Long meetingId) {
//    List<PaymentResponse> payments = paymentService.getPaymentsByMeetingId(meetingId);
//    return ResponseEntity.ok(ApiResponse.success("모임별 결제 내역 조회가 완료되었습니다.", payments));
//  }
//
//  // 사용자별 결제 내역 조회
//  @GetMapping("/users/{userId}")
//  public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUser(
//      @PathVariable Long userId) {
//    List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
//    return ResponseEntity.ok(ApiResponse.success("사용자별 결제 내역 조회가 완료되었습니다.", payments));
//  }


}
