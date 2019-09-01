package com.transaction.services;

import com.mongodb.MongoWriteException;
import com.transaction.model.Account;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

public class MongoService<T> {
  private final String documentName;
  @Inject
  @Named("MongoClient")
  private MongoClient client;

  public MongoService(String documentName) {
    this.documentName = documentName;
  }

  public Future<JsonObject> getById(String id) {
    JsonObject query = new JsonObject();
    query.put("accountNumber", id);

    Future<JsonObject> future = Future.future();

    client.find(documentName, query, res -> {
      if (res.succeeded()) {
        List<JsonObject> objects = res.result();
        if (objects.size() > 0) {
          future.complete(objects.get(0));
        } else {
          future.fail("No result");
        }
      } else {
        future.fail(res.cause());
      }
    });

    return future;
  }

  public Future<JsonObject> save(T model) {
    Future<JsonObject> future = Future.future();
    JsonObject document = new JsonObject(Json.encodePrettily(model));
    client.save(documentName, document, res -> {
      if (res.succeeded()) {
        if (document.getString("_id") == null) {
          document.put("_id", res.result());
        }
        future.complete(document);
      } else {
        if (res.cause() instanceof MongoWriteException && ((MongoWriteException) res.cause()).getCode() == 11000) {
          future.fail("Duplicate entry not allowed");
        } else {
          future.fail(res.cause());
        }
      }
    });
    return future;
  }

  public Future<JsonObject> update(T model) {
    Future<JsonObject> future = Future.future();
    JsonObject document = new JsonObject(Json.encodePrettily(model));
    client.updateCollection(documentName,
      new JsonObject().put("accountNumber", document.getString("accountNumber")),
      new JsonObject()
        .put("$set", document),
      res -> {
        if (res.succeeded()) {
          future.complete(document);
        } else {
          if (res.cause() instanceof MongoWriteException && ((MongoWriteException) res.cause()).getCode() == 11000) {
            future.fail("Duplicate entry not allowed");
          } else {
            future.fail(res.cause());
          }
        }
      });
    return future;
  }
}
