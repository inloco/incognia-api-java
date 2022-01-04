package com.incognia.requests;

import com.incognia.types.PaymentType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentMethod {
  PaymentType type;
  CardInfo creditCardInfo;
  CardInfo debitCardInfo;
}
