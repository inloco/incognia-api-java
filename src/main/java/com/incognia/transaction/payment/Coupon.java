package com.incognia.transaction.payment;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Coupon {
  String type;
  Double value;
  Double maxDiscount;
  String id;
  String name;
}
