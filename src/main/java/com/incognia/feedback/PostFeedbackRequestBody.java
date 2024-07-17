package com.incognia.feedback;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostFeedbackRequestBody {
  FeedbackEvent event;
  @Deprecated Long timestamp;
  Instant occurredAt;
  String accountId;
  String externalId;
  String installationId;
  String sessionToken;
  String paymentId;
  String loginId;
  String signupId;
}
