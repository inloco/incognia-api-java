package com.incognia;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class TransactionAddress {
  String type;
  String addressLine;
  StructuredAddress structuredAddress;
  Coordinates addressCoordinates;
}
