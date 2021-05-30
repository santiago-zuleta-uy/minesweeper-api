package com.minesweeper.api.gameservice;

import com.minesweeper.api.builders.GameBuilder;
import com.minesweeper.api.builders.RoutingContextMockBuilder;
import com.minesweeper.api.models.Cell;
import com.minesweeper.api.models.Game;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_FIND_GAME_BY_ID;
import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_UPSERT_GAME;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameServiceGameWonTest {

  @BeforeAll
  public void beforeAll(Vertx vertx) {
    this.registerCodecs(vertx);
  }

  private void registerCodecs(Vertx vertx) {
    vertx.eventBus().registerDefaultCodec(Game.class, new GenericMessageCodec<>(Game.class));
    vertx.eventBus().registerDefaultCodec(User.class, new GenericMessageCodec<>(User.class));
    vertx.eventBus().registerDefaultCodec(ArrayList.class, new GenericMessageCodec<>(ArrayList.class));
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
}
