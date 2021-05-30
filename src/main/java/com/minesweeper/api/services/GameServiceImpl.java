package com.minesweeper.api.services;

import com.minesweeper.api.builders.GameBuilder;
import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.models.Cell;
import com.minesweeper.api.models.CellFlag;
import com.minesweeper.api.models.Game;
import com.minesweeper.api.models.GameStatus;
import com.minesweeper.api.models.User;
import com.minesweeper.api.utils.GameUtil;
import com.minesweeper.api.utils.RoutingContextUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_FIND_GAME_BY_ID;
import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_FIND_USER_BY_EMAIL;
import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_UPSERT_GAME;

public class GameServiceImpl implements GameService {

  private final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

  private final Vertx vertx;

  public GameServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void createGame(RoutingContext routingContext) {
    Integer rows = routingContext.getBodyAsJson().getInteger("rows", 0);
    Integer columns = routingContext.getBodyAsJson().getInteger("columns", 0);
    Integer mines = routingContext.getBodyAsJson().getInteger("mines", 0);
    String userEmail = routingContext.get("userEmail");
    this.vertx.eventBus().<User>request(REPOSITORY_FIND_USER_BY_EMAIL.address, userEmail).onSuccess(userMessage -> {
      User user = userMessage.body();
      Game game = GameBuilder.get()
        .withColumns(columns)
        .withRows(rows)
        .withMines(mines)
        .withUserEmail(user.getEmail())
        .build();
      this.upsertGameInRepository(routingContext, game);
    });
  }

  @Override
  public void getGame(RoutingContext routingContext) {
    String id = routingContext.pathParam("gameId");
    this.vertx.eventBus().<Game>request(EventBusAddress.REPOSITORY_FIND_GAME_BY_ID.address, id, response -> {
      if (response.failed()) {
        RoutingContextUtil.respondInternalServerError(routingContext);
      } else {
        Game game = response.result().body();
        if (game == null) {
          RoutingContextUtil.respondNotFound(routingContext);
        } else {
          game.updateSecondsPlayedIfInProgress();
          RoutingContextUtil.respondSuccess(routingContext, JsonObject.mapFrom(game));
        }
      }
    });
  }

  @Override
  public void revealGameCell(RoutingContext routingContext) {
    String gameId = routingContext.pathParam("gameId");
    String row = routingContext.getBodyAsJson().getString("row");
    String column = routingContext.getBodyAsJson().getString("column");
    this.getFindGameByIdFuture(gameId).onSuccess(message -> {
      Game game = message.body();
      if (game == null) {
        RoutingContextUtil.respondNotFound(routingContext);
      } else if (game.getStatus().isGameOver() || game.getStatus().isGameWon()) {
        logger.info("unable to reveal cell for finished game " + gameId);
        RoutingContextUtil.respondSuccess(routingContext, JsonObject.mapFrom(game));
      } else {
        this.revealCellAndUpdateGame(routingContext, game, row, column);
      }
    }).onFailure(throwable -> {
      logger.error("failed to reveal cell for game " + gameId, throwable);
      RoutingContextUtil.respondInternalServerError(routingContext);
    });
  }

  private void revealCellAndUpdateGame(RoutingContext routingContext, Game game, String row, String column) {
    Map<String, Cell> cellsMap = game.getCells();
    Cell cell = cellsMap.get(row + ":" + column);
    if (cell == null) {
      RoutingContextUtil.respondNotFound(routingContext);
    } else if (cell.isMined()) {
      game.updateSecondsPlayedIfInProgress();
      game.setStatus(GameStatus.GAME_OVER);
      this.upsertGameInRepository(routingContext, game);
      RoutingContextUtil.respondSuccess(routingContext, JsonObject.mapFrom(game));
    } else {
      Game updatedGame = this.revealCellAndUpdateGame(game, cell);
      this.upsertGameInRepository(routingContext, updatedGame);
    }
  }

  private Game revealCellAndUpdateGame(Game game, Cell cell) {
    Set<Cell> revealedCells = GameUtil.revealCellsRecursively(game, cell);
    game.putCells(revealedCells);
    game.resumeIfPaused();
    if (GameUtil.isGameWon(game)) {
      game.setStatus(GameStatus.GAME_WON);
    }
    game.updateSecondsPlayedIfInProgress();
    return game;
  }

  private void upsertGameInRepository(RoutingContext routingContext, Game game) {
    this.getUpsertGameFuture(game).onSuccess(response -> {
      String gameId = response.body();
      Game upsertedGame = game.setId(gameId);
      logger.info("successfully upserted game " + upsertedGame.getId());
      RoutingContextUtil.respondSuccess(routingContext, JsonObject.mapFrom(upsertedGame));
    }).onFailure(throwable -> {
      logger.error("failed to upsert game " + game.getId(), throwable);
      RoutingContextUtil.respondInternalServerError(routingContext);
    });
  }

  @Override
  public void flagGameCell(RoutingContext routingContext) {
    String gameId = routingContext.pathParam("gameId");
    String row = routingContext.getBodyAsJson().getString("row");
    String column = routingContext.getBodyAsJson().getString("column");
    String flagType = routingContext.getBodyAsJson().getString("flagType");
    CellFlag flag = CellFlag.get(flagType);
    if (flag == null) {
      RoutingContextUtil.respondBadRequest(routingContext, "Invalid flag type");
    }
    this.getFindGameByIdFuture(gameId).onSuccess(message -> {
      Game game = message.body();
      if (game == null) {
        RoutingContextUtil.respondNotFound(routingContext);
      } else {
        Cell cell = game.getCells().get(row + ":" + column);
        Cell flaggedCell = cell.setFlag(flag);
        game.putCell(flaggedCell);
        game.resumeIfPaused();
        this.getUpsertGameFuture(game).onSuccess(saveGameResponse -> {
          logger.info("cell " + cell.key() + " successfully flagged for game " + gameId);
          RoutingContextUtil.respondSuccess(routingContext, JsonObject.mapFrom(game));
        }).onFailure(throwable -> {
          logger.error("failed to flag cell for game " + gameId, throwable);
          RoutingContextUtil.respondInternalServerError(routingContext);
        });
      }
    });
  }

  @Override
  public void pauseGame(RoutingContext routingContext) {
    String gameId = routingContext.pathParam("gameId");
    this.getFindGameByIdFuture(gameId).onSuccess(message -> {
      Game game = message.body();
      if (game == null) {
        RoutingContextUtil.respondNotFound(routingContext);
      } else if (game.getStatus().isNotGameInProgress()) {
        String responseMessage = "game " + gameId + " is invalid status for being paused";
        logger.info(responseMessage);
        RoutingContextUtil.respondBadRequest(routingContext, responseMessage);
      } else {
        game.updateSecondsPlayedIfInProgress();
        game.setStatus(GameStatus.GAME_PAUSED);
        this.getUpsertGameFuture(game).onSuccess(saveGameResponse -> {
          logger.info("game " + gameId + " successfully paused");
          RoutingContextUtil.respondSuccess(routingContext, JsonObject.mapFrom(game));
        }).onFailure(throwable -> {
          logger.error("failed to pause game " + gameId, throwable);
          RoutingContextUtil.respondInternalServerError(routingContext);
        });
      }
    });
  }

  private Future<Message<String>> getUpsertGameFuture(Game game) {
    return this.vertx.eventBus().request(REPOSITORY_UPSERT_GAME.address, game);
  }

  private Future<Message<Game>> getFindGameByIdFuture(String gameId) {
    return this.vertx.eventBus().request(REPOSITORY_FIND_GAME_BY_ID.address, gameId);
  }

  /**
   * Just a logging method to help myself troubleshooting issues locally.
   *
   * @param game
   */
  private void logGame(Game game) {
    List<String> keys = game.getCells().keySet().stream().sorted().collect(Collectors.toList());
    long lastRow = 0;
    System.out.print("\n");
    for (String key : keys) {
      Cell c = game.getCells().get(key);
      if (c.getRow() != lastRow) {
        lastRow = c.getRow();
        System.out.print("\n");
      }
      System.out.print(c.log());
    }
    System.out.print("\n");
  }
}
