package com.incognia.requests.body;

import com.incognia.requests.Coordinates;
import com.incognia.requests.StructuredAddress;
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
