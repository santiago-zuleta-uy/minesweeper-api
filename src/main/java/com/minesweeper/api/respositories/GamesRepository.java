package com.minesweeper.api.respositories;

import com.minesweeper.api.models.Game;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.mongo.MongoClient;

public abstract class GamesRepository {

  protected MongoClient mongoClient;

  public GamesRepository(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public abstract void upsertGame(Message<Game> message);
  public abstract void findGameById(Message<String> message);
  public abstract void findGameByUserEmail(Message<String> message);
}
