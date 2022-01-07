package com.incognia.transaction;

import com.incognia.api.Assessment;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TransactionAssessment {
  UUID id;
  Assessment riskAssessment;
  Map<String, Object> evidence;
  String deviceId;
}
