package com.incognia.transaction.payment;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentMethod {
  PaymentType type;
  CardInfo creditCardInfo;
  CardInfo debitCardInfo;
}
