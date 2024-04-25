package com.incognia.api.clients;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

public class ObjectMapperFactory {
  @SuppressWarnings("deprecation")
  // PropertyNamingStrategy.SNAKE_CASE is deprecated but we use it for compatibility with older
  // jackson versions
  public static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
          .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
          .setSerializationInclusion(Include.NON_NULL)
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
