package com.incognia;

import java.util.Map;
import java.util.UUID;
import lombok.Value;

@Value
public class SignupResponse {
  UUID id;
  UUID requestId;
  Assessment riskAssessment;
  Map<String, Object> evidence;
}
