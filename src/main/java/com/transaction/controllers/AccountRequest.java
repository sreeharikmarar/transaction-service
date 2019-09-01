package com.transaction.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountRequest {

  @JsonProperty
  private String accountNumber;

  public AccountRequest(){}

  @JsonCreator
  public AccountRequest(@JsonProperty String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public String getAccountNumber() {
    return accountNumber;
  }
}
