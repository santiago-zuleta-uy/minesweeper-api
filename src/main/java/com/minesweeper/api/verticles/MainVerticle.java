package com.minesweeper.api.verticles;

import com.minesweeper.api.models.Game;
import com.minesweeper.api.models.User;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

import java.util.ArrayList;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {
    registerCodecs();
    deployVerticle(WebServerVerticle.class);
    deployVerticle(EmbeddedMongoVerticle.class);
    deployVerticle(SecurityVerticle.class);
  }

  private void deployVerticle(Class<? extends AbstractVerticle> verticleClass) {
    this.vertx.deployVerticle(
      verticleClass,
      new DeploymentOptions().setInstances(1)
    );
  }

  private void registerCodecs() {
    this.vertx.eventBus().registerDefaultCodec(Game.class, new GenericMessageCodec<>(Game.class));
    this.vertx.eventBus().registerDefaultCodec(User.class, new GenericMessageCodec<>(User.class));
    this.vertx.eventBus().registerDefaultCodec(ArrayList.class, new GenericMessageCodec<>(ArrayList.class));
  }
}
