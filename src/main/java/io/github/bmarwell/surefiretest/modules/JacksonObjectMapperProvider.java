package io.github.bmarwell.surefiretest.modules;

import com.fasterxml.jackson.databind.ObjectMapper;

public enum JacksonObjectMapperProvider {
  INSTANCE;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public static ObjectMapper getInstance() {
    return INSTANCE.objectMapper;
  }
}
