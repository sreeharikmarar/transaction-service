package com.transaction.services;

import com.mongodb.MongoWriteException;
import com.transaction.model.Account;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.List;

public class MongoService<T> {
  private final String documentName;
  @Inject
  @Named("MongoClient")
  private MongoClient client;

  public MongoService(String documentName) {
    this.documentName = documentName;
  }

  public Future<Account> getByField(String id) {
    JsonObject query = new JsonObject();
    query.put("accountNumber", id);

    Future<Account> future = Future.future();

    client.find(documentName, query, res -> {
      if (res.succeeded()) {
        List<JsonObject> objects = res.result();
        Account account = null;
        if (objects.size() > 0) {
          JsonObject data = objects.get(0);
          account = Account.builder()
            .withAccountNumber(data.getString("accountNumber"))
            .withBalance(BigDecimal.valueOf(data.getDouble("balance")))
            .build();
        }
        future.complete(account);
      } else {
        future.fail(res.cause());
      }
    });

    return future;
  }

  public Future<JsonObject> create(T model) {
    Future<JsonObject> future = Future.future();
    JsonObject document = new JsonObject(Json.encode(model));
    client.save(documentName, document, res -> {
      if (res.succeeded()) {
        if (document.getString("_id") == null) {
          document.put("_id", res.result());
        }
        future.complete(document);
      } else {
        if (res.cause() instanceof MongoWriteException && ((MongoWriteException) res.cause()).getCode() == 11000) {
          future.fail("Duplicate entry not allowed");
        }else{
          future.fail(res.cause());
        }
      }
    });
    return future;
  }
}
