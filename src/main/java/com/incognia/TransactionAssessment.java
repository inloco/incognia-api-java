package com.incognia;

import java.util.Map;
import java.util.UUID;
import lombok.Value;

@Value
public class TransactionAssessment {
  UUID id;
  Assessment riskAssessment;
  Map<String, Object> evidence;
}
