package com.incognia.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HolderTaxID implements TypedValue {
  String type;
  String value;

  public static HolderTaxID ofCPF(String cpfValue) {
    return HolderTaxID.builder().type("cpf").value(cpfValue).build();
  }

  public static HolderTaxID ofSSN(String ssnValue) {
    return HolderTaxID.builder().type("ssn").value(ssnValue).build();
  }

  public static HolderTaxID ofEIN(String einValue) {
    return HolderTaxID.builder().type("ein").value(einValue).build();
  }

  public static HolderTaxID ofCNPJ(String cnpjValue) {
    return HolderTaxID.builder().type("cnpj").value(cnpjValue).build();
  }
}
