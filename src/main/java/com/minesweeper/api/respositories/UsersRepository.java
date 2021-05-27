package com.minesweeper.api.respositories;

import com.minesweeper.api.models.User;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.mongo.MongoClient;

public abstract class UsersRepository {

  protected MongoClient mongoClient;

  public UsersRepository(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public abstract void createUser(Message<User> message);
  public abstract void findUserByEmail(Message<String> message);
}
