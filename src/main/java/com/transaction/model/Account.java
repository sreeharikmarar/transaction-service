package com.transaction.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account {
  private String _id;
  private String accountNumber;
  private BigDecimal balance;

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String accountNumber;
    private BigDecimal balance = BigDecimal.ZERO;

    private Builder() {
    }

    public Builder withAccountNumber(String accountNumber) {
      this.accountNumber = accountNumber;
      return this;
    }

    public Builder withBalance(BigDecimal balance) {
      this.balance = balance;
      return this;
    }

    public Account build() {
      Account account = new Account();
      account.balance = balance;
      account.accountNumber = this.accountNumber;
      return account;
    }
  }

  @Override
  public String toString() {
    return "Account{" +
      "_id='" + _id + '\'' +
      ", accountNumber='" + accountNumber + '\'' +
      ", balance=" + balance +
      '}';
  }
}
