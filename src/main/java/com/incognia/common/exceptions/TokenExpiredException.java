package com.incognia.common.exceptions;

public class TokenExpiredException extends IncogniaException {
  public TokenExpiredException() {
    super("token is expired");
  }
}
