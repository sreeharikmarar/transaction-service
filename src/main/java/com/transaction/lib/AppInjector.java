package com.transaction.lib;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class AppInjector extends AbstractModule {
    private Vertx vertx;

    public AppInjector(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    protected void configure() {
        JsonObject mongoConfig = new JsonObject()
            .put("connection_string", "mongodb://localhost:27017")
            .put("db_name", "test");

        MongoClient mongo = MongoClient.createShared(vertx, mongoConfig);
        bind(MongoClient.class).annotatedWith(Names.named("MongoClient")).toInstance(mongo);
    }
}
