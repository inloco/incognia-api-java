package com.incognia.common;

import com.incognia.transaction.payment.PixKey;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FinancialAccount {
  String accountNumber;
  String branchCode;
  HolderTaxID holderTaxID;
  String holderType;
  String accountCheckDigit;
  String accountPurpose;
  String accountType;
  String country; // ISO 3166-1 alpha-2
  String ispbCode;
  List<PixKey> pixKeys;
}
