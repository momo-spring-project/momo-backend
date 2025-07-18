package com.example.momo.domain.payments.api;

import com.example.momo.domain.payments.application.PaymentService;
import com.example.momo.domain.payments.dto.PaymentRequest;
import com.example.momo.domain.payments.dto.PaymentResponse;
import com.example.momo.domain.payments.dto.RefundRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 요청 (v1: 바로 완료 처리)
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    // 결제 내역 조회
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByMeeting(@PathVariable Long meetingId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByMeetingId(meetingId);
        return ResponseEntity.ok(payments);
    }

    // 사용자별 결제 내역 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUser(@PathVariable Long userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    // 환불 처리
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long paymentId,
            @RequestBody RefundRequest request) {
        PaymentResponse response = paymentService.refundPayment(paymentId, request);
        return ResponseEntity.ok(response);
    }
}