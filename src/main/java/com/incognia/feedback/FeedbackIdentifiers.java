package com.incognia.feedback;

import com.incognia.common.PersonID;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FeedbackIdentifiers {
  String installationId;
  String sessionToken;
  String requestToken;
  String loginId;
  String paymentId;
  String signupId;
  String accountId;
  String externalId;
  Instant expiresAt;
  PersonID personId;
}
