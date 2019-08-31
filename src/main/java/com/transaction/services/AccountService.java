package com.transaction.services;

import com.transaction.model.Account;

public class AccountService  extends MongoService<Account> {
  AccountService() {
    super("accounts");
  }
}
