package com.incognia.feedback;

import com.incognia.common.PersonID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostFeedbackRequestBody {
  FeedbackEvent event;
  Long timestamp;
  String accountId;
  String externalId;
  String installationId;
  String sessionToken;
  String requestToken;
  String paymentId;
  String loginId;
  String signupId;
  String expiresAt;
  PersonID personId;
}
