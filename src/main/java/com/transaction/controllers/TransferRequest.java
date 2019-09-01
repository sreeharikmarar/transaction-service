package com.transaction.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferRequest {
  @JsonProperty
  private String fromAccount;

  @JsonProperty
  private String toAccount;

  @JsonProperty
  private Float amount;

  public TransferRequest() {
  }

  public TransferRequest(@JsonProperty String fromAccount,
                         @JsonProperty String toAccount,
                         @JsonProperty Float amount) {
    this.fromAccount = fromAccount;
    this.toAccount = toAccount;
    this.amount = amount;
  }

  public String getFromAccount() {
    return fromAccount;
  }

  public String getToAccount() {
    return toAccount;
  }

  public Float getAmount() {
    return amount;
  }
}
