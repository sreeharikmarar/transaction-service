package com.transaction.controllers;

import com.google.inject.Inject;
import com.transaction.model.Account;
import com.transaction.services.AccountService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.math.BigDecimal;

public class AccountController {
  @Inject
  private AccountService service;

  public void getAccount(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    HttpServerResponse response = routingContext.response();
    service.getById(id).setHandler(r -> {
      if (r.succeeded()) {
        response
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(new JsonObject()
            .put("accountNumber", r.result().getString("accountNumber"))
            .put("balance", r.result().getFloat("balance"))
            .encode()
          );
      } else {
        response.putHeader("content-type", "application/json; charset=utf-8");
        response.setStatusCode(400);
        response.end(new JsonObject()
          .put("timestamp", System.nanoTime())
          .put("error", "Invalid Request")
          .put("message", "Invalid AccountNumber")
          .encode());
      }
    });
  }

  public void createAccount(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    AccountRequest request = null;

    try {
      request = Json.decodeValue(routingContext.getBodyAsString(), AccountRequest.class);
    } catch (io.vertx.core.json.DecodeException e) {
      response.putHeader("content-type", "application/json; charset=utf-8");
      response.setStatusCode(400);
      response.end(new JsonObject()
        .put("timestamp", System.nanoTime())
        .put("error", "Invalid Request")
        .put("message", e.getMessage())
        .encode());
    }

    Account account = Account.builder()
      .withAccountNumber(request.getAccountNumber())
      .build();

    service.getById(account.getAccountNumber()).setHandler(r -> {
      if (r.succeeded()) {
        response.putHeader("content-type", "application/json; charset=utf-8");
        response.setStatusCode(400);
        response.end(new JsonObject()
          .put("timestamp", System.nanoTime())
          .put("error", "Invalid Request")
          .put("message", "Account already exists")
          .encode());
      } else {
        service.save(account).setHandler(res -> {
          if (res.succeeded()) {
            response.putHeader("content-type", "application/json; charset=utf-8")
              .end(new JsonObject()
                .put("accountNumber", res.result().getString("accountNumber"))
                .put("balance", res.result().getFloat("balance"))
                .encode()
              );
          } else {
            response.setStatusCode(500);
            response.end(new JsonObject()
              .put("timestamp", System.nanoTime())
              .put("error", "Invalid Request")
              .put("message", "Server Error")
              .encode());
          }
        });
      }
    });
  }

  public void updateAccount(RoutingContext routingContext) {
    String accountNumber = routingContext.request().getParam("id");
    HttpServerResponse response = routingContext.response();
    AccountUpdateRequest request = null;

    try {
      request = Json.decodeValue(routingContext.getBodyAsString(), AccountUpdateRequest.class);
    } catch (io.vertx.core.json.DecodeException e) {
      response.putHeader("content-type", "application/json; charset=utf-8");
      response.setStatusCode(400);
      response.end(new JsonObject()
        .put("timestamp", System.nanoTime())
        .put("error", "Invalid Request")
        .put("message", e.getMessage())
        .encode());
    }

    final AccountUpdateRequest finalRequest = request;

    service.getById(accountNumber).setHandler(r -> {
      if (!r.succeeded()) {
        response.putHeader("content-type", "application/json; charset=utf-8");
        response.setStatusCode(400);
        response.end(new JsonObject()
          .put("timestamp", System.nanoTime())
          .put("error", "Invalid Request")
          .put("message", "Account doesnt exists")
          .encode());
      } else {
        Account account = Json.decodeValue(r.result().toString(), Account.class);
        account.setBalance(finalRequest.getBalance());
        updateAccountDetails(response, account);
      }
    });
  }


  public void transferAmount(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    TransferRequest request = null;
    try {
      request = Json.decodeValue(routingContext.getBodyAsString(), TransferRequest.class);
    } catch (io.vertx.core.json.DecodeException e) {
      response.putHeader("content-type", "application/json; charset=utf-8");
      response.setStatusCode(400);
      response.end(new JsonObject()
        .put("timestamp", System.nanoTime())
        .put("error", "Invalid Request")
        .put("message", e.getMessage())
        .encode());
    }

    final TransferRequest finalRequest = request;

    service.getById(finalRequest.getFromAccount()).setHandler(fromAccount -> {
      if (!fromAccount.succeeded()) {
        response.putHeader("content-type", "application/json; charset=utf-8");
        response.setStatusCode(400);
        response.end(new JsonObject()
          .put("timestamp", System.nanoTime())
          .put("error", "Invalid Request")
          .put("message", "Invalid FromAccount")
          .encode());
      } else {
        service.getById(finalRequest.getToAccount()).setHandler(toAccount -> {
          if (!toAccount.succeeded()) {
            response.putHeader("content-type", "application/json; charset=utf-8");
            response.setStatusCode(400);
            response.end(new JsonObject()
              .put("timestamp", System.nanoTime())
              .put("error", "Invalid Request")
              .put("message", "Invalid ToAccount")
              .encode());
          } else {
            Account fromAcnt = Json.decodeValue(fromAccount.result().toString(), Account.class);

            if ((fromAcnt
              .getBalance()
              .subtract(BigDecimal.valueOf(finalRequest.getAmount()))
              .doubleValue()) < 0) {

              response.putHeader("content-type", "application/json; charset=utf-8");
              response.setStatusCode(400);
              response.end(new JsonObject()
                .put("timestamp", System.nanoTime())
                .put("error", "Invalid Request")
                .put("message", "Insufficient Fund")
                .encode());
            } else {
              Account fa = Json.decodeValue(fromAccount.result().toString(), Account.class);
              Account ta = Json.decodeValue(toAccount.result().toString(), Account.class);

              fa.setBalance(
                fa
                  .getBalance()
                  .subtract(BigDecimal.valueOf(finalRequest.getAmount())));

              ta.setBalance(
                ta
                  .getBalance()
                  .add(BigDecimal.valueOf(finalRequest.getAmount()))
              );

              service.update(fa).setHandler(res -> {
                if (res.succeeded()) {
                  updateAccountDetails(response, fa);
                }
              });
            }
          }
        });
      }
    });
  }

  private void updateAccountDetails(HttpServerResponse response, Account account) {
    service.update(account).setHandler(re -> {
      if (re.succeeded()) {
        response.putHeader("content-type", "application/json; charset=utf-8");
        response.end(new JsonObject()
          .put("accountNumber", re.result().getString("accountNumber"))
          .put("balance", re.result().getFloat("balance"))
          .encode());
      } else {
        response.end(new JsonObject()
          .put("timestamp", System.nanoTime())
          .put("error", "Invalid Request")
          .put("message", "System Error")
          .encode());
      }
    });
  }
}
