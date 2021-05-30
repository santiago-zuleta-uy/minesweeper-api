package com.minesweeper.api.gameservice;

import com.minesweeper.api.builders.RoutingContextMockBuilder;
import com.minesweeper.api.builders.GameBuilder;
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

import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_FIND_GAME_BY_ID;
import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_UPSERT_GAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameServiceRevealSingleGameCellTest {

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
