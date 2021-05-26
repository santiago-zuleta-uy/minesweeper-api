package com.minesweeper.api.respositories;

import com.minesweeper.api.constants.MongoDbCollection;
import com.minesweeper.api.models.Game;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class GamesRepositoryImpl extends GamesRepository {

  private static final Logger logger = LoggerFactory.getLogger(GamesRepositoryImpl.class);

  public GamesRepositoryImpl(MongoClient mongoClient) {
    super(mongoClient);
  }

  @Override
  public void createGame(Message<Game> message) {
    JsonObject document = JsonObject.mapFrom(message.body());
    logger.trace("saving game: " + document);
    this.mongoClient.save(
      MongoDbCollection.GAMES.name,
      document,
      response -> {
        if (response.failed()) {
          message.fail(500, response.cause().getMessage());
        }
        logger.trace("game saved: " + document);
        message.reply(response.result());
      });
  }

  @Override
  public void findGameById(Message<String> message) {
    this.mongoClient.findOne(
      MongoDbCollection.GAMES.name,
      new JsonObject().put("_id", message.body()),
      new JsonObject(),
      response -> {
        if (response.failed()) {
          message.fail(500, response.cause().getMessage());
        }
        message.reply(response.result());
      }
    );
  }
}
