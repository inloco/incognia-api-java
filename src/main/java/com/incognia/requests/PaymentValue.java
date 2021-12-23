package com.incognia.requests;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentValue {
  Double amount;
  String currency;
}
