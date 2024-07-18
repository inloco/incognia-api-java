package com.incognia.feedback;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostFeedbackRequestBody {
  String event;
  @Deprecated Long timestamp;
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
