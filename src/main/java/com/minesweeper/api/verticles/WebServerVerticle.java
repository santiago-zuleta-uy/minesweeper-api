package com.minesweeper.api.verticles;

import com.minesweeper.api.controllers.DocumentationController;
import com.minesweeper.api.controllers.GamesController;
import com.minesweeper.api.controllers.HealthCheckController;
import com.minesweeper.api.controllers.UsersController;
import com.minesweeper.api.utils.RoutingContextUtil;
import io.vertx.ext.web.Router;

public class WebServerVerticle extends AbstractWebServerVerticle {

  @Override
  protected void routes(Router router) {
    HealthCheckController healthCheckController = new HealthCheckController();
    GamesController gamesController = new GamesController(this.vertx);
    UsersController usersController = new UsersController(this.vertx);
    DocumentationController documentationController = new DocumentationController();
    router.get("/")
      .handler(healthCheckController::health);
    router.get("/v1/documentation")
      .handler(documentationController::getDocumentation);
    router.post("/v1/users")
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullBodyParam(rc, "email"))
      .handler(usersController::authenticateUser);
    router.post("/v1/games")
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullBodyParam(rc, "rows"))
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullBodyParam(rc, "columns"))
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullBodyParam(rc, "mines"))
      .handler(usersController::validateUser)
      .handler(gamesController::createGame);
    router.get("/v1/games/:gameId")
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullPathParam(rc, "gameId"))
      .handler(usersController::validateUser)
      .handler(usersController::validateUserGame)
      .handler(gamesController::getGame);
    router.patch("/v1/games/:gameId/cells/reveal")
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullPathParam(rc, "gameId"))
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullBodyParam(rc, "row"))
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullBodyParam(rc, "column"))
      .handler(usersController::validateUser)
      .handler(usersController::validateUserGame)
      .handler(gamesController::revealGameCell);
    router.patch("/v1/games/:gameId/cells/flag")
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullPathParam(rc, "gameId"))
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullBodyParam(rc, "row"))
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullBodyParam(rc, "column"))
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullBodyParam(rc, "flagType"))
      .handler(usersController::validateUser)
      .handler(usersController::validateUserGame)
      .handler(gamesController::flagGameCell);
    router.patch("/v1/games/:gameId/pause")
      .handler(rc -> RoutingContextUtil.respondBadRequestIfNullPathParam(rc, "gameId"))
      .handler(usersController::validateUser)
      .handler(usersController::validateUserGame)
      .handler(gamesController::pauseGame);
  }
}
