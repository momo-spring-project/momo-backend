package com.example.momo.domain.payments.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardPaymentTestRequest {


  @NotNull
  private Long meetingId;

  private String cardNumber;   // 기본: 4242424242424242
  private String cardExpiry;   // 12/25
  private String cardCvc;      // 242
  private String birth;        // 881212
}