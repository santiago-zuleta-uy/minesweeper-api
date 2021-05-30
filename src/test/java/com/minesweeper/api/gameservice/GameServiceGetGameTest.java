package com.minesweeper.api.gameservice;

import com.minesweeper.api.builders.RoutingContextMockBuilder;
import com.minesweeper.api.builders.GameBuilder;
import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.models.Game;
import com.minesweeper.api.models.User;
import com.minesweeper.api.services.GameService;
import com.minesweeper.api.services.GameServiceImpl;
import com.minesweeper.api.verticles.GenericMessageCodec;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameServiceGetGameTest {

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
}
