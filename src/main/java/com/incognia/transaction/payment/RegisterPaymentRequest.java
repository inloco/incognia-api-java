package com.incognia.transaction.payment;

import com.incognia.common.Address;
import com.incognia.common.Location;
import com.incognia.common.PersonID;
import com.incognia.transaction.AddressType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class RegisterPaymentRequest {
  String installationId;
  String requestToken;
  String appVersion;
  String deviceOs;
  String accountId;
  String externalId;
  String policyId;
  String storeId;
  @Builder.Default Map<AddressType, Address> addresses = Collections.emptyMap();
  @Builder.Default List<PaymentMethod> paymentMethods = Collections.emptyList();
  @Builder.Default Map<String, Object> customProperties = Collections.emptyMap();
  PaymentValue paymentValue;
  Location location;
  Coupon coupon;
  PersonID personId;

  @Getter(AccessLevel.NONE)
  Boolean evaluateTransaction;

  public Boolean shouldEvaluateTransaction() {
    return this.evaluateTransaction;
  }
}
