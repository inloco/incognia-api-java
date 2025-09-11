package com.incognia.transaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.incognia.common.Location;
import com.incognia.common.PersonID;
import com.incognia.transaction.payment.Coupon;
import com.incognia.transaction.payment.PaymentMethod;
import com.incognia.transaction.payment.PaymentValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostTransactionRequestBody {
  String installationId;
  String requestToken;
  String appVersion;
  String deviceOs;
  String accountId;
  String sessionToken;
  String policyId;
  String type;
  String storeId;
  String externalId;
  String relatedAccountId;
  Location location;
  Coupon coupon;
  PersonID personId;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  List<TransactionAddress> addresses = Collections.emptyList();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  PaymentValue paymentValue;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  List<PaymentMethod> paymentMethods = Collections.emptyList();

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  Map<String, Object> customProperties = Collections.emptyMap();
}
