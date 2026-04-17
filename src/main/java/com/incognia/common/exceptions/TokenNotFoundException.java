package com.incognia.common.exceptions;

public class TokenNotFoundException extends IncogniaException {
  public TokenNotFoundException() {
    super("token not found in memory");
  }
}
