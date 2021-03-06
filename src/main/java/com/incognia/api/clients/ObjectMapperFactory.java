package com.incognia.api.clients;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class ObjectMapperFactory {
  public static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
          .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .setSerializationInclusion(Include.NON_NULL);
}
