package com.minesweeper.api;

import com.minesweeper.api.verticles.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    Checkpoint checkpoint = testContext.checkpoint(1);
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> checkpoint.flag()));
    TimeUnit.SECONDS.sleep(2);
  }
}
