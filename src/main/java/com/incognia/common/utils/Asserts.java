package com.incognia.common.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Asserts {
  /**
   * Asserts that a string is not null or empty
   *
   * @param value the value to check.
   * @param name the name of the parameter, used when creating the exception message.
   * @throws IllegalArgumentException if the value is null or empty (length = 0)
   */
  public static void assertNotEmpty(String value, String name) {
    if (value == null || value.length() == 0) {
      throw new IllegalArgumentException(String.format("'%s' cannot be empty", name));
    }
  }

  /**
   * Asserts that a value is not null
   *
   * @param value the value to check.
   * @param name the name of the parameter, used when creating the exception message.
   * @throws IllegalArgumentException if the value is null
   */
  public static void assertNotNull(Object value, String name) {
    if (value == null) {
      throw new IllegalArgumentException(String.format("'%s' cannot be null", name));
    }
  }
}
