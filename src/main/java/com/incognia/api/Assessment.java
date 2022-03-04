package com.incognia.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Assessment {
  @JsonProperty("low_risk")
  LOW_RISK,
  @JsonProperty("high_risk")
  HIGH_RISK,
  @JsonProperty("unknown_risk")
  UNKNOWN_RISK;
}
