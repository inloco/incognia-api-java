package com.incognia.onboarding;

import com.incognia.api.Assessment;
import com.incognia.common.Reason;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Value;

@Value
public class SignupAssessment {
  UUID id;
  UUID requestId;
  Assessment riskAssessment;
  Set<Reason> reasons;
  Map<String, Object> evidence;
  String deviceId;
  String installationId;
}
