package com.minesweeper.api.services;

import io.vertx.ext.web.RoutingContext;

public interface UserService {
  void createUser(RoutingContext routingContext);
}
