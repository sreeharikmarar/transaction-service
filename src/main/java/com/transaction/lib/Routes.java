package com.transaction.lib;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.transaction.controllers.AccountController;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Routes {
  public Router getRoutes(Vertx vertx) {
    Router router = Router.router(vertx);

    router.route().failureHandler(ctx -> {
      final JsonObject json = new JsonObject()
        .put("timestamp", System.nanoTime())
        .put("status", ctx.statusCode())
        .put("error", HttpResponseStatus.valueOf(ctx.statusCode()).reasonPhrase());

      final String message = ctx.get("message");

      if (message != null) {
        json.put("message", message);
      }
      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
      ctx.response().end(json.encodePrettily());
    });

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
      router.post("/api/accounts").handler(account::createAccount);
      router.put("/api/accounts/:id").handler(account::updateAccount);

      router.post("/api/accounts/transfer").handler(account::transferAmount);
      future.complete(router);
    }, res -> {
      System.out.println("Routes injected in verticle successfully");
    });

    return router;
  }
}
