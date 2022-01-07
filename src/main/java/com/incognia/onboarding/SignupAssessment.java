package com.incognia.onboarding;

import com.incognia.api.Assessment;
import java.util.Map;
import java.util.UUID;
import lombok.Value;

@Value
public class SignupAssessment {
  UUID id;
  UUID requestId;
  Assessment riskAssessment;
  Map<String, Object> evidence;
  String deviceId;
}
