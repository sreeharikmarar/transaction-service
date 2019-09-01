package com.transaction.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

public class MoneySerializer extends JsonSerializer<BigDecimal> {
  @Override
  public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    System.out.println(">>>>>> inside seria");
    jgen.writeString(value.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
  }
}
