package com.transaction.lib;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.transaction.controllers.AccountController;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Routes {
  public Router getRoutes(Vertx vertx) {
    Router router = Router.router(vertx);
    router.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response
        .putHeader("content-type", "text/html")
        .end("Welcome");
    });

    vertx.executeBlocking(future -> {
      Injector i = Guice.createInjector(new AppInjector(vertx));
      AccountController account = i.getInstance(AccountController.class);
      router.route("/api/*").handler(BodyHandler.create());
      router.get("/api/accounts/:id").handler(account::getAccount);
      future.complete(router);
    }, res -> {
      System.out.println("Routes injected in verticle successfully");
    });

    return router;
  }
}
