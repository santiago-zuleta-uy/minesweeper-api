package com.minesweeper.api.controllers;

import com.minesweeper.api.services.GameService;
import com.minesweeper.api.services.GameServiceImpl;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class GamesController {

  private GameService gameService;

  public GamesController(Vertx vertx) {
    this.gameService = new GameServiceImpl(vertx);
  }

  public void createGame(RoutingContext routingContext) {
    this.gameService.createGame(routingContext);
  }

  public void getGame(RoutingContext routingContext) {
    this.gameService.getGame(routingContext);
  }

  public void revealGameCell(RoutingContext routingContext) {
    this.gameService.revealGameCell(routingContext);
  }

  public void flagGameCell(RoutingContext routingContext) {
    this.gameService.flagGameCell(routingContext);
  }

  public void pauseGame(RoutingContext routingContext) {
    this.gameService.pauseGame(routingContext);
  }
}
