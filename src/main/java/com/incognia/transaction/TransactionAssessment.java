package com.incognia.transaction;

import com.incognia.api.Assessment;
import com.incognia.common.Reason;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TransactionAssessment {
  UUID id;
  Assessment riskAssessment;
  List<Reason> reasons;
  Map<String, Object> evidence;
  String deviceId;
  String installationId;
}
