package com.minesweeper.api.verticles;

import com.minesweeper.api.controllers.GamesController;
import com.minesweeper.api.controllers.HealthCheckController;
import com.minesweeper.api.controllers.UsersController;
import com.minesweeper.api.services.GameServiceImpl;
import com.minesweeper.api.services.UserServiceImpl;
import io.vertx.ext.web.Router;

public class WebServerVerticle extends AbstractWebServerVerticle {


  @Override
  protected void routes(Router router) {
    HealthCheckController healthCheckController = new HealthCheckController();
    GamesController gamesController = new GamesController(new GameServiceImpl(this.vertx));
    UsersController usersController = new UsersController(new UserServiceImpl(this.vertx));
    router.get("/").handler(healthCheckController::health);
    router.post("/v1/users/:userEmail/games").handler(gamesController::createGame);
    router.post("/v1/users").handler(usersController::createUser);
    router.get("/v1/games/:gameId").handler(gamesController::getGame);
    router.patch("/v1/games/:gameId/reveal").handler(gamesController::revealGameCell);
  }
}
