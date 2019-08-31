package com.transaction.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account {
  private String _id;
  private String accountNumber;
  private BigDecimal balance;

  public String get_id() {
    return _id;
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
    private BigDecimal balance;

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
      account.accountNumber = this.accountNumber;
      account.balance = this.balance;
      return account;
    }

    @Override
    public String toString() {
      return "{" +
        ", accountNumber='" + accountNumber + '\'' +
        ", balance=" + balance +
        '}';
    }
  }
}
