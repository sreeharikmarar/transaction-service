package com.transaction.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class AccountUpdateRequest {

  @JsonProperty
  private BigDecimal balance;

  public BigDecimal getBalance() {
    return balance;
  }
}


