package com.minesweeper.api.controllers;

import com.minesweeper.api.services.UserService;
import com.minesweeper.api.services.UserServiceImpl;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class UsersController {

  private final UserService userService;

  public UsersController(Vertx vertx) {
    this.userService = new UserServiceImpl(vertx);
  }

  public void authenticateUser(RoutingContext routingContext) {
    this.userService.authenticateUser(routingContext);
  }

  public void validateUser(RoutingContext routingContext) {
    this.userService.validateUser(routingContext);
  }

  public void validateUserGame(RoutingContext routingContext) {
    this.userService.validateUserGame(routingContext);
  }
}
