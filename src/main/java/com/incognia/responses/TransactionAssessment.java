package com.incognia.responses;

import com.incognia.types.Assessment;
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
