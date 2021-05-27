package com.minesweeper.api.controllers;

import com.minesweeper.api.services.UserService;
import io.vertx.ext.web.RoutingContext;

public class UsersController {

  private final UserService gameService;

  public UsersController(UserService gameService) {
    this.gameService = gameService;
  }

  public void createUser(RoutingContext routingContext) {
    this.gameService.createUser(routingContext);
  }
}
