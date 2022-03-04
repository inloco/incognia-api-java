package com.incognia.transaction.payment;

import com.incognia.common.Address;
import com.incognia.transaction.AddressType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
public class RegisterPaymentRequest {
  String installationId;
  String accountId;
  String externalId;
  @Builder.Default Map<AddressType, Address> addresses = Collections.emptyMap();
  @Builder.Default List<PaymentMethod> paymentMethods = Collections.emptyList();
  PaymentValue paymentValue;

  @Getter(AccessLevel.NONE)
  Boolean evaluateTransaction;

  public Boolean shouldEvaluateTransaction() {
    return this.evaluateTransaction;
  }
}
