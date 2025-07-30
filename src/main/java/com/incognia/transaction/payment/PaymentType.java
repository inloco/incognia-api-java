package com.incognia.transaction.payment;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {
  ACCOUNT_BALANCE("account_balance"),
  APPLE_PAY("apple_pay"),
  BANCOLOMBIA("bancolombia"),
  BOLETO_BANCARIO("boleto_bancario"),
  CASH("cash"),
  CREDIT_CARD("credit_card"),
  DEBIT_CARD("debit_card"),
  GOOGLE_PAY("google_pay"),
  MEAL_VOUCHER("meal_voucher"),
  NU_PAY("nu_pay"),
  PAYPAL("paypal"),
  PIX("pix");
  private final String message;

  @JsonValue
  public String getMessage() {
    return message;
  }
}
