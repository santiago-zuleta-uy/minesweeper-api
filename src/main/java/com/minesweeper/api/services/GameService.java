package com.minesweeper.api.services;

import io.vertx.ext.web.RoutingContext;

public interface GameService {
  void createGame(RoutingContext routingContext);
  void getGame(RoutingContext routingContext);
}
