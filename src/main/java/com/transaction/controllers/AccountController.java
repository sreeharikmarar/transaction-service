package com.transaction.controllers;

import com.google.inject.Inject;
import com.transaction.model.Account;
import com.transaction.services.AccountService;
import io.vertx.core.AsyncResult;
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
        sendError(response, "Invalid AccountNumber");
      }
    });
  }

  public void createAccount(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    AccountRequest request = null;

    try {
      request = Json.decodeValue(routingContext.getBodyAsString(), AccountRequest.class);
    } catch (io.vertx.core.json.DecodeException e) {
      sendError(response, e.getMessage());
    }

    Account account = Account.builder()
      .withAccountNumber(request.getAccountNumber())
      .build();

    service.getById(account.getAccountNumber()).setHandler(r -> {
      if (r.succeeded()) {
        sendError(response, "Account already exists");
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
            sendServerError(response, res);
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
      sendError(response, e.getMessage());
    }

    final AccountUpdateRequest finalRequest = request;

    service.getById(accountNumber).setHandler(r -> {
      if (!r.succeeded()) {
        sendError(response, "Account doesnt exists");
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
      sendError(response, e.getMessage());
    }

    final TransferRequest finalRequest = request;

    service.getById(finalRequest.getFromAccount()).setHandler(fromAccount -> {
      if (!fromAccount.succeeded()) {
        sendError(response, "Invalid FromAccount");
      } else {
        service.getById(finalRequest.getToAccount()).setHandler(toAccount -> {
          if (!toAccount.succeeded()) {
            sendError(response, "Invalid ToAccount");
          } else {
            Account fromAcnt = Json.decodeValue(fromAccount.result().toString(), Account.class);

            if ((fromAcnt
              .getBalance()
              .subtract(BigDecimal.valueOf(finalRequest.getAmount()))
              .doubleValue()) < 0) {

              sendError(response, "Insufficient Fund");
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
                  updateFromAndToAccountDetails(response, ta, res);
                } else {
                  sendServerError(response, res);
                }
              });
            }
          }
        });
      }
    });
  }

  private void updateFromAndToAccountDetails(HttpServerResponse response, Account toAccount, AsyncResult<JsonObject> res) {
    service.update(toAccount).setHandler(re -> {
      if (re.succeeded()) {
        response.putHeader("content-type", "application/json; charset=utf-8");
        response.end(new JsonObject()
          .put("accountNumber", res.result().getString("accountNumber"))
          .put("balance", res.result().getFloat("balance"))
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

  private void sendServerError(HttpServerResponse response, AsyncResult<JsonObject> res) {
    response.setStatusCode(500);
    response.end(new JsonObject()
      .put("timestamp", System.nanoTime())
      .put("error", "Server Error")
      .put("message", res.cause())
      .encode());
  }

  private void sendError(HttpServerResponse response, String s) {
    response.putHeader("content-type", "application/json; charset=utf-8");
    response.setStatusCode(400);
    response.end(new JsonObject()
      .put("timestamp", System.nanoTime())
      .put("error", "Invalid Request")
      .put("message", s)
      .encode());
  }
}
