package com.minesweeper.api.services;

import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.models.Game;
import com.minesweeper.api.models.GameBuilder;
import com.minesweeper.api.utils.RoutingContextUtil;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class GameServiceImpl implements GameService {

  private Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

  private Vertx vertx;

  public GameServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void createGame(RoutingContext routingContext) {
    logger.info("creating game");
    Integer rows = routingContext.getBodyAsJson().getInteger("rows", 0);
    Integer columns = routingContext.getBodyAsJson().getInteger("columns", 0);
    Integer mines = routingContext.getBodyAsJson().getInteger("mines", 0);
    String userId = routingContext.queryParams().get("userId");
    Game game = GameBuilder.get()
      .withColumns(columns)
      .withRows(rows)
      .withMines(mines)
      .withUserId(userId)
      .build();
    vertx.eventBus().request(EventBusAddress.REPOSITORY_CREATE_GAME.address, game, response -> {
      if (response.failed()) {
        RoutingContextUtil.respondInternalServerError(routingContext);
      }
      String id = (String) response.result().body();
      RoutingContextUtil.respondSuccess(routingContext, new JsonObject().put("id", id));
      logger.info("game created");
    });
  }

  @Override
  public void getGame(RoutingContext routingContext) {
    String id = routingContext.pathParam("gameId");
    vertx.eventBus().request(EventBusAddress.REPOSITORY_FIND_GAME_BY_ID.address, id, response -> {
      if (response.failed()) {
        RoutingContextUtil.respondInternalServerError(routingContext);
      }
      JsonObject result = (JsonObject) response.result().body();
      RoutingContextUtil.respondSuccess(routingContext, result);
    });
  }
}
