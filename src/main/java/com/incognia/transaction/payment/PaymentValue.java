package com.incognia.transaction.payment;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentValue {
  Double amount;
  String currency;
}
