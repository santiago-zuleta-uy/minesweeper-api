package com.minesweeper.api.services;

import io.vertx.ext.web.RoutingContext;

public interface UserService {
  void authenticateUser(RoutingContext routingContext);
  void validateUserGame(RoutingContext routingContext);
  void validateUser(RoutingContext routingContext);
}
