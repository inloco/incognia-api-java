package com.incognia.common.exceptions;

public class IncogniaException extends Exception {
  public IncogniaException(String message) {
    super(message);
  }

  public IncogniaException(String message, Throwable cause) {
    super(message, cause);
  }
}
