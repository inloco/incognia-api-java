package com.incognia.transaction;

import com.incognia.common.Coordinates;
import com.incognia.common.StructuredAddress;
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
