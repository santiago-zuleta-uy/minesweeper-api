package com.minesweeper.api.services;

import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.models.*;
import com.minesweeper.api.utils.RoutingContextUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minesweeper.api.constants.EventBusAddress.*;

public class GameServiceImpl implements GameService {

  private Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

  private Vertx vertx;

  private UserService userService;

  public GameServiceImpl(Vertx vertx) {
    this.vertx = vertx;
    this.userService = new UserServiceImpl(vertx);
  }

  @Override
  public void createGame(RoutingContext routingContext) {
    Integer rows = routingContext.getBodyAsJson().getInteger("rows", 0);
    Integer columns = routingContext.getBodyAsJson().getInteger("columns", 0);
    Integer mines = routingContext.getBodyAsJson().getInteger("mines", 0);
    String userEmail = routingContext.pathParam("userEmail");
    this.vertx.eventBus().<User>request(REPOSITORY_FIND_USER_BY_EMAIL.address, userEmail).onSuccess(userMessage -> {
      User user = userMessage.body();
      Game game = GameBuilder.get()
        .withColumns(columns)
        .withRows(rows)
        .withMines(mines)
        .withUserEmail(user.getEmail())
        .build();
      this.vertx.eventBus().<String>request(REPOSITORY_SAVE_GAME.address, game).onSuccess(gameMessage -> {
        String id = gameMessage.body();
        RoutingContextUtil.respondSuccess(routingContext, new JsonObject().put("id", id));
      });
    });
  }

  @Override
  public void getGame(RoutingContext routingContext) {
    String id = routingContext.pathParam("gameId");
    vertx.eventBus().<Game>request(EventBusAddress.REPOSITORY_FIND_GAME_BY_ID.address, id, response -> {
      if (response.failed()) {
        RoutingContextUtil.respondInternalServerError(routingContext);
      } else {
        Game game = response.result().body();
        RoutingContextUtil.respondSuccess(routingContext, JsonObject.mapFrom(game));
      }
    });
  }

  @Override
  public void revealGameCell(RoutingContext routingContext) {
    String gameId = routingContext.pathParam("gameId");
    String row = routingContext.queryParams().get("row");
    String column = routingContext.queryParams().get("column");
    this.getFindGameByIdFuture(gameId).onSuccess(message -> {
      Game game = message.body();
      Map<String, Cell> cellsMap = game.getCells();
      Cell cell = cellsMap.get(row + ":" + column);
      if (cell.isMined()) {
        game.setGameStatus(GameStatus.GAME_OVER);
      } else {
        List<Cell> revealedCells = this.revealAndGetAdjacentNonMinedCells(game, cell);
        revealedCells.forEach(revealedCell -> {
          String key = revealedCell.getRow() + ":" + revealedCell.getColumn();
          game.getCells().put(key, revealedCell);
        });
      }
      this.getSaveGameFuture(game).onSuccess(saveGameResponse -> {
        RoutingContextUtil.respondSuccess(routingContext, JsonObject.mapFrom(game));
      }).onFailure(throwable -> {
        logger.error("failed to reveal cell for game " + gameId, throwable);
        RoutingContextUtil.respondInternalServerError(routingContext);
      });
    }).onFailure(throwable -> {
      logger.error("failed to reveal cell for game " + gameId, throwable);
      RoutingContextUtil.respondInternalServerError(routingContext);
    });
  }

  private List<Cell> revealAndGetAdjacentNonMinedCells(Game game, Cell cell) {
    cell.setRevealed(true);
    List<Cell> cells = getAdjacentNonMinedCells(game, cell);
    if (cells.isEmpty()) {
      return cells;
    } else {
      for (Cell adjacentCell : cells) {
        cells.addAll(revealAndGetAdjacentNonMinedCells(game, adjacentCell));
      }
    }
    return cells;
  }

  private List<Cell> getAdjacentNonMinedCells(Game game, Cell cell) {
    List<String> adjacentCells = new ArrayList<>();
    for (int row = cell.getRow() - 1; row <= cell.getRow(); row++) {
      for (int column = cell.getColumn() - 1; column <= cell.getColumn(); column++) {
        boolean isNotTargetCell = row != cell.getRow() && column != cell.getColumn();
        if (isNotTargetCell &&
          this.existsCell(game, row, column) &&
          this.isNotRevealedCell(game, row, column)
        ) {
          adjacentCells.add(row + ":" + column);
        }
      }
    }
    adjacentCells.forEach(c -> System.out.println(c.toString()));
    boolean mineDetectedOnAdjacent = adjacentCells.stream()
      .map(key -> game.getCells().get(key))
      .anyMatch(c -> c.isMined() && !c.isFlagged());
    if (mineDetectedOnAdjacent) {
      return Collections.emptyList();
    } else {
      return adjacentCells.stream()
        .map(key -> game.getCells().get(key))
        .map(c -> c.setRevealed(true))
        .collect(Collectors.toList());
    }
  }

  private boolean isNotRevealedCell(Game game, int row, int column) {
    Cell cell = game.getCells().get(row + ":" + column);
    if (cell == null) {
      return false;
    } else {
      return !cell.isRevealed();
    }
  }

  private boolean existsCell(Game game, int row, int column) {
    return row >= 0 && column >= 0 && row < game.getRows() && column < game.getColumns();
  }

  private Future<Message<String>> getSaveGameFuture(Game game) {
    return this.vertx.eventBus().request(REPOSITORY_SAVE_GAME.address, game);
  }

  private Future<Message<Game>> getFindGameByIdFuture(String gameId) {
    return this.vertx.eventBus().request(REPOSITORY_FIND_GAME_BY_ID.address, gameId);
  }
}
