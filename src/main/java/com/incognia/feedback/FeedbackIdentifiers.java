package com.incognia.feedback;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FeedbackIdentifiers {
  String installationId;
  String sessionToken;
  String loginId;
  String paymentId;
  String signupId;
  String accountId;
  String externalId;
}
