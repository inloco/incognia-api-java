package com.incognia;

import java.io.IOException;

public class IncogniaException extends IOException {
  public IncogniaException(String message) {
    super(message);
  }

  public IncogniaException(String message, Throwable cause) {
    super(message, cause);
  }
}
