package com.incognia.common.exceptions;

import java.util.Map;

public class IncogniaAPIException extends IncogniaException {
  private final int statusCode;
  private final Map<String, Object> responsePayload;

  public IncogniaAPIException(int statusCode, Map<String, Object> responsePayload) {
    this(statusCode, responsePayload, null);
  }

  public IncogniaAPIException(
      int statusCode, Map<String, Object> responsePayload, Throwable cause) {
    super(String.format("Incognia API request failed with status code %d", statusCode), cause);
    this.statusCode = statusCode;
    this.responsePayload = responsePayload;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Map<String, Object> getResponsePayload() {
    return responsePayload;
  }
}
