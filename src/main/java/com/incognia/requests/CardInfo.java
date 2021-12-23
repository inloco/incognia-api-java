package com.incognia.requests;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CardInfo {
  String bin;
  String lastFourDigits;
  String expiryYear;
  String expiryMonth;
}
