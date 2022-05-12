package com.incognia.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReasonSource {
  LOCAL("local"),
  GLOBAL("global");

  private final String source;

  public boolean isSameAs(String source) {
    return this.source.equals(source);
  }
}
