package com.incognia.common;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FinancialAccount {
  String accountNumber;
  String branchCode;
  PersonID holderTaxId;
  String holderType;
  String accountCheckDigit;
  String accountPurpose;
  String accountType;
  String country;
  String ispbCode;
  List<PixKey> pixKeys;
}