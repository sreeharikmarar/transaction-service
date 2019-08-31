package com.transaction.controllers;

import com.transaction.services.AccountService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import com.google.inject.Inject;

public class AccountController {
  @Inject
  private AccountService service;

  public void getAccount(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    service.getByField(id).setHandler(r -> {
      routingContext
        .response()
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(new JsonObject()
          .put("accountNumber", r.result().getAccountNumber())
          .put("balance", r.result().getBalance().floatValue())
          .encode()
        );
    });
  }

  public void createAccount(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    service.getByField(id).setHandler(r -> {
      routingContext
        .response()
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(new JsonObject()
          .put("accountNumber", r.result().getAccountNumber())
          .put("balance", r.result().getBalance().floatValue())
          .encode()
        );
    });
  }
}
