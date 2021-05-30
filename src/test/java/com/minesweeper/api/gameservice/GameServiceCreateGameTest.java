package com.minesweeper.api.gameservice;

import com.minesweeper.api.builders.RoutingContextMockBuilder;
import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.models.Cell;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameServiceCreateGameTest {

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
}
