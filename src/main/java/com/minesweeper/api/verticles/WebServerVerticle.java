package com.minesweeper.api.verticles;

import com.minesweeper.api.controllers.GamesController;
import com.minesweeper.api.controllers.HealthCheckController;
import com.minesweeper.api.services.GameServiceImpl;
import io.vertx.ext.web.Router;

public class WebServerVerticle extends AbstractWebServerVerticle {


  @Override
  protected void routes(Router router) {
    HealthCheckController healthCheckController = new HealthCheckController();
    GamesController gamesController = new GamesController(new GameServiceImpl(this.vertx));
    router.get("/").handler(healthCheckController::health);
    router.post("/v1/games").handler(gamesController::createGame);
    router.get("/v1/games/:gameId").handler(gamesController::getGame);
  }
}