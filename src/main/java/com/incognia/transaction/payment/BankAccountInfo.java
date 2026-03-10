package com.incognia.transaction.payment;

import com.incognia.common.HolderTaxID;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BankAccountInfo {
  String accountType;
  String accountPurpose;
  String holderType;
  HolderTaxID holderTaxId;
  String country;
  String ispbCode;
  String branchCode;
  String accountNumber;
  String accountCheckDigit;
  @Builder.Default List<PixKey> pixKeys = Collections.emptyList();
}
