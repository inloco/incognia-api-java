package com.incognia.types;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {
  CREDIT_CARD("credit_card"),
  DEBIT_CARD("debit_card");

  private final String message;

  @JsonValue
  public String getMessage() {
    return message;
  }
}
