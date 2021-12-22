package com.incognia;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostTransactionRequestBody {
  String installationId;
  String accountId;

  String type;
  String externalId;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  List<TransactionAddress> addresses = Collections.emptyList();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  PaymentValue paymentValue;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  List<PaymentMethod> paymentMethods = Collections.emptyList();
}
