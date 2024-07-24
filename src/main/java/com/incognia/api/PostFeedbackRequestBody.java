package com.incognia.api;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class PostFeedbackRequestBody {
  String event;
  Instant occurredAt;
  Instant expiresAt;
  String accountId;
  String externalId;
  String installationId;
  String sessionToken;
  String requestToken;
  String paymentId;
  String loginId;
  String signupId;
}
