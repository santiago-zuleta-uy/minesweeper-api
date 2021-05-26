package com.minesweeper.api.controllers;

import com.minesweeper.api.services.GameService;
import io.vertx.ext.web.RoutingContext;

public class GamesController {

  private GameService gameService;

  public GamesController(GameService gameService) {
    this.gameService = gameService;
  }

  public void createGame(RoutingContext routingContext) {
    this.gameService.createGame(routingContext);
  }

  public void getGame(RoutingContext routingContext) {
    this.gameService.getGame(routingContext);
  }
}
