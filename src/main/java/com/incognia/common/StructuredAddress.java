package com.incognia.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StructuredAddress {
  String locale;
  String countryName;
  String countryCode;
  String state;
  String city;
  String borough;
  String neighborhood;
  String street;
  String number;
  String complements;
  String postalCode;
}
