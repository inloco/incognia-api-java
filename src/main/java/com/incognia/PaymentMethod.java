package com.incognia;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentMethod {
  String type;

  CardInfo creditCardInfo;
  CardInfo debitCardInfo;
}
