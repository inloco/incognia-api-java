package com.incognia.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PersonID {
  String type;
  String value;

  public static PersonID ofCPF(String cpfValue) {
    return PersonID.builder().type("cpf").value(cpfValue).build();
  }
}
