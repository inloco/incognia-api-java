package com.incognia;

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
  @Builder.Default List<TransactionAddress> addresses = Collections.emptyList();
}
