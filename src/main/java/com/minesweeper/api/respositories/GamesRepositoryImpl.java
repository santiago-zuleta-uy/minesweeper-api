package com.minesweeper.api.respositories;

import com.minesweeper.api.constants.MongoDbCollection;
import com.minesweeper.api.models.Game;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.List;
import java.util.stream.Collectors;

public class GamesRepositoryImpl extends GamesRepository {

  private static final Logger logger = LoggerFactory.getLogger(GamesRepositoryImpl.class);

  public GamesRepositoryImpl(MongoClient mongoClient) {
    super(mongoClient);
  }

  @Override
  public void updateGame(Message<Game> message) {
    Game game = message.body();
    JsonObject gameJson = JsonObject.mapFrom(game);
    gameJson.remove("_id");
    JsonObject document = new JsonObject().put("$set", gameJson);
    JsonObject query = new JsonObject().put("_id", game.getId());
    logger.info("updating game: " + game.getId());
    this.mongoClient.updateCollectionWithOptions(
      MongoDbCollection.GAMES.name,
      query,
      document,
      new UpdateOptions().setUpsert(true)
    ).onComplete(response -> {
      if (response.failed()) {
        logger.error("failed to upsert game " + game.getId(), response.cause());
        message.fail(500, response.cause().getMessage());
      } else {
        logger.info("successfully updated game " + game.getId());
        message.reply(game.getId());
      }
    });
  }

  @Override
  public void findGameById(Message<String> message) {
    String gameId = message.body();
    this.mongoClient.findOne(
      MongoDbCollection.GAMES.name,
      new JsonObject().put("_id", gameId),
      new JsonObject(),
      response -> {
        if (response.failed()) {
          logger.error("failed to find game " + gameId, response.cause());
          message.fail(500, response.cause().getMessage());
        } else {
          JsonObject result = response.result();
          message.reply(result.mapTo(Game.class));
        }
      }
    );
  }

  @Override
  public void findGameByUserEmail(Message<String> message) {
    String userEmail = message.body();
    this.mongoClient.find(
      MongoDbCollection.GAMES.name,
      new JsonObject().put("userEmail", userEmail),
      response -> {
        if (response.failed()) {
          logger.error("failed to find games for user email " + userEmail, response.cause());
          message.fail(500, response.cause().getMessage());
        } else {
          List<JsonObject> results = response.result();
          message.reply(
            results.stream()
              .map(result -> result.mapTo(Game.class))
              .collect(Collectors.toList())
          );
        }
      }
    );
  }
}
