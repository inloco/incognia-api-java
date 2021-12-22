package com.incognia;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardType {
  CREDIT_CARD("credit_card"),
  DEBIT_CARD("debit_card");

  private final String message;

  @JsonValue
  public String getMessage() {
    return message;
  }
}
