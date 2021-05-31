package com.minesweeper.api.gameservice;

import com.minesweeper.api.builders.GameBuilder;
import com.minesweeper.api.builders.RoutingContextMockBuilder;
import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.models.Cell;
import com.minesweeper.api.models.CellFlag;
import com.minesweeper.api.models.Game;
import com.minesweeper.api.models.GameStatus;
import com.minesweeper.api.models.User;
import com.minesweeper.api.services.GameService;
import com.minesweeper.api.services.GameServiceImpl;
import com.minesweeper.api.verticles.GenericMessageCodec;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_FIND_GAME_BY_ID;
import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_UPSERT_GAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class GameServiceTest {

  @BeforeEach
  public void beforeAll(Vertx vertx) {
    this.registerCodecs(vertx);
  }

  private void registerCodecs(Vertx vertx) {
    vertx.eventBus().registerDefaultCodec(Game.class, new GenericMessageCodec<>(Game.class));
    vertx.eventBus().registerDefaultCodec(User.class, new GenericMessageCodec<>(User.class));
    vertx.eventBus().registerDefaultCodec(ArrayList.class, new GenericMessageCodec<>(ArrayList.class));
  }

  @Test
  public void createGameSuccessfully(Vertx vertx, VertxTestContext vertxTestContext) {
    User user = new User().setEmail("test@mail.com");
    JsonObject body = new JsonObject()
      .put("rows", 2)
      .put("columns", 2)
      .put("mines", 1);
    vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_FIND_USER_BY_EMAIL.address,
      (Message<String> message) -> message.reply(user)
    );
    vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_UPSERT_GAME.address,
      (Message<Game> message) -> message.reply(message.body().getId())
    );
    GameService gameService = new GameServiceImpl(vertx);
    RoutingContext routingContext = RoutingContextMockBuilder.of()
      .withBodyParameter(body)
      .withUserEmailContextValue(user.getEmail())
      .build();
    gameService.createGame(routingContext);

    ArgumentCaptor<Buffer> responseCaptor = ArgumentCaptor.forClass(Buffer.class);
    verify(routingContext.response(), timeout(1000).times(1)).end(responseCaptor.capture());

    Game gameCreated = Json.decodeValue(responseCaptor.getValue(), Game.class);

    assertNotNull(gameCreated.getId());
    long rows = body.getLong("rows");
    long columns = body.getLong("columns");
    long cells = rows * columns;
    assertEquals(rows, gameCreated.getRows());
    assertEquals(columns, gameCreated.getColumns());
    assertEquals(cells, gameCreated.getCells().size());
    assertEquals(user.getEmail(), gameCreated.getUserEmail());
    assertEquals(GameStatus.GAME_IN_PROGRESS, gameCreated.getStatus());
    assertEquals(1, gameCreated.getCells().values().stream().filter(Cell::isMined).count());

    vertxTestContext.completeNow();
  }

  @Test
  public void flagGameCellWithQuestionMark(Vertx vertx, VertxTestContext vertxTestContext) {
    Game game = GameBuilder.of()
      .withColumns(10)
      .withRows(10)
      .withMines(10)
      .build();

    JsonObject body = new JsonObject()
      .put("row", 3)
      .put("column", 4)
      .put("flagType", CellFlag.QUESTION_MARK);

    GameService gameService = new GameServiceImpl(vertx);
    RoutingContext routingContext = RoutingContextMockBuilder.of()
      .withBodyParameter(body)
      .withGameIdPathParameter(game.getId())
      .build();

    vertx.eventBus().consumer(
      REPOSITORY_FIND_GAME_BY_ID.address,
      (Message<String> message) -> message.reply(game)
    );
    vertx.eventBus().consumer(
      REPOSITORY_UPSERT_GAME.address,
      (Message<Game> message) -> message.reply(message.body().getId())
    );

    gameService.flagGameCell(routingContext);

    ArgumentCaptor<Buffer> responseCaptor = ArgumentCaptor.forClass(Buffer.class);
    verify(routingContext.response(), timeout(1000).times(1)).end(responseCaptor.capture());

    Game gameWithFlaggedCell = Json.decodeValue(responseCaptor.getValue(), Game.class);

    assertEquals(game.getId(), gameWithFlaggedCell.getId());
    assertEquals(game.getRows(), gameWithFlaggedCell.getRows());
    assertEquals(game.getColumns(), gameWithFlaggedCell.getColumns());
    assertEquals(game.getCells().size(), gameWithFlaggedCell.getCells().size());

    Cell cell = gameWithFlaggedCell.getCells().get("3:4");
    assertTrue(cell.getFlag().isQuestionMark());

    vertxTestContext.completeNow();
  }

  @Test
  public void gameWonAfterRevealGameCells(Vertx vertx, VertxTestContext vertxTestContext) {
    Game game = GameBuilder.of()
      .withColumns(2)
      .withRows(2)
      .withMines(1)
      .build();

    vertx.eventBus().consumer(
      REPOSITORY_FIND_GAME_BY_ID.address,
      (Message<String> message) -> message.reply(game)
    );

    vertx.eventBus().consumer(
      REPOSITORY_UPSERT_GAME.address,
      (Message<Game> message) -> message.reply(message.body().getId())
    );

    GameService gameService = new GameServiceImpl(vertx);

    List<Cell> nonMinedCells = game.getCells()
      .values()
      .stream()
      .filter(cell -> !cell.isMined())
      .collect(Collectors.toList());

    List<Game> gamesWithCellsRevealed = new ArrayList<>();

    nonMinedCells.forEach(nonMinedCell -> {
      RoutingContext routingContext = RoutingContextMockBuilder.of()
        .withBodyParameter(new JsonObject().put("row", nonMinedCell.getRow()).put("column", nonMinedCell.getColumn()))
        .withGameIdPathParameter(game.getId())
        .build();
      gameService.revealGameCell(routingContext);
      ArgumentCaptor<Buffer> responseCaptor = ArgumentCaptor.forClass(Buffer.class);
      verify(routingContext.response(), timeout(1000).times(1)).end(responseCaptor.capture());
      Game gameWithCellRevealed = Json.decodeValue(responseCaptor.getValue(), Game.class);
      gamesWithCellsRevealed.add(gameWithCellRevealed);
    });

    assertTrue(
      gamesWithCellsRevealed.stream().anyMatch(gameWithCellRevealed -> gameWithCellRevealed.getStatus().isGameWon())
    );

    vertxTestContext.completeNow();
  }

  @Test
  public void getGameSuccessfully(Vertx vertx, VertxTestContext vertxTestContext) {
    Game game = GameBuilder.of()
      .withColumns(10)
      .withRows(10)
      .withMines(5)
      .build();
    GameService gameService = new GameServiceImpl(vertx);
    RoutingContext routingContext = RoutingContextMockBuilder.of()
      .withGameIdPathParameter(game.getId())
      .build();

    vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_FIND_GAME_BY_ID.address,
      (Message<String> message) -> message.reply(game)
    );

    gameService.getGame(routingContext);

    ArgumentCaptor<Buffer> responseCaptor = ArgumentCaptor.forClass(Buffer.class);
    verify(routingContext.response(), timeout(1000).times(1)).end(responseCaptor.capture());

    Game gameFound = Json.decodeValue(responseCaptor.getValue(), Game.class);

    assertEquals(game.getId(), gameFound.getId());
    assertEquals(game.getRows(), gameFound.getRows());
    assertEquals(game.getColumns(), gameFound.getColumns());
    assertEquals(game.getCells().size(), gameFound.getCells().size());

    vertxTestContext.completeNow();
  }

  @Test
  public void pauseGame(Vertx vertx, VertxTestContext vertxTestContext) {
    Game game = GameBuilder.of()
      .withColumns(10)
      .withRows(10)
      .withMines(10)
      .build();

    GameService gameService = new GameServiceImpl(vertx);
    RoutingContext routingContext = RoutingContextMockBuilder.of()
      .withGameIdPathParameter(game.getId())
      .build();

    vertx.eventBus().consumer(
      REPOSITORY_FIND_GAME_BY_ID.address,
      (Message<String> message) -> message.reply(game)
    );
    vertx.eventBus().consumer(
      REPOSITORY_UPSERT_GAME.address,
      (Message<Game> message) -> message.reply(message.body().getId())
    );

    gameService.pauseGame(routingContext);

    ArgumentCaptor<Buffer> responseCaptor = ArgumentCaptor.forClass(Buffer.class);
    verify(routingContext.response(), timeout(1000).times(1)).end(responseCaptor.capture());

    Game pausedGame = Json.decodeValue(responseCaptor.getValue(), Game.class);

    assertNotNull(pausedGame);
    assertTrue(pausedGame.getStatus().isGamePaused());

    vertxTestContext.completeNow();
  }

  @Test
  public void revealSingleGameCell(Vertx vertx, VertxTestContext vertxTestContext) {
    Game game = GameBuilder.of()
      .withColumns(10)
      .withRows(10)
      .withMines(10)
      .build();

    JsonObject body = new JsonObject()
      .put("row", 3)
      .put("column", 4);

    GameService gameService = new GameServiceImpl(vertx);
    RoutingContext routingContext = RoutingContextMockBuilder.of()
      .withBodyParameter(body)
      .withGameIdPathParameter(game.getId())
      .build();

    vertx.eventBus().consumer(
      REPOSITORY_FIND_GAME_BY_ID.address,
      (Message<String> message) -> message.reply(game)
    );
    vertx.eventBus().consumer(
      REPOSITORY_UPSERT_GAME.address,
      (Message<Game> message) -> message.reply(message.body().getId())
    );

    gameService.revealGameCell(routingContext);

    ArgumentCaptor<Buffer> responseCaptor = ArgumentCaptor.forClass(Buffer.class);
    verify(routingContext.response(), timeout(1000).times(1)).end(responseCaptor.capture());

    Game gameWithCellRevealed = Json.decodeValue(responseCaptor.getValue(), Game.class);

    assertEquals(game.getId(), gameWithCellRevealed.getId());
    assertEquals(game.getRows(), gameWithCellRevealed.getRows());
    assertEquals(game.getColumns(), gameWithCellRevealed.getColumns());
    assertEquals(game.getCells().size(), gameWithCellRevealed.getCells().size());

    Cell cell = gameWithCellRevealed.getCells().get("3:4");
    if (cell.isMined()) {
      assertTrue(game.getStatus().isGameOver());
    } else {
      assertTrue(cell.isRevealed());
    }

    vertxTestContext.completeNow();
  }
}
