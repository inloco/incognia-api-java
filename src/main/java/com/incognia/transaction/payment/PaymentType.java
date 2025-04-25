package com.incognia.transaction.payment;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {
  CREDIT_CARD("credit_card"),
  DEBIT_CARD("debit_card"),
  APPLE_PAY("apple_pay"),
  GOOGLE_PAY("google_pay"),
  NU_PAY("nu_pay"),
  PIX("pix"),
  ACCOUNT_BALANCE("account_balance"),
  MEAL_VOUCHER("meal_voucher"),
  CASH("cash"),
  PAYPAL("paypal"),
  BANCOLOMBIA("bancolombia");

  private final String message;

  @JsonValue
  public String getMessage() {
    return message;
  }
}
