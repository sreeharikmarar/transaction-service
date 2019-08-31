package com.transaction.lib;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class App extends AbstractVerticle {

  @Override
  public void start(Future<Void> future) {
    vertx
      .createHttpServer()
      .requestHandler(new Routes().getRoutes(vertx)::accept)
      .listen(8888, result -> {
        if (result.succeeded()) {
          future.complete();
        } else {
          future.fail(result.cause());
        }
      });
  }
}
