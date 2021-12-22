package com.incognia;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentValue {
  Double amount;
  String currency;
}
