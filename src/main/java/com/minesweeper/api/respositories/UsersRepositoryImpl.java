package com.minesweeper.api.respositories;

import com.minesweeper.api.constants.MongoDbCollection;
import com.minesweeper.api.models.User;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class UsersRepositoryImpl extends UsersRepository {

  private static final Logger logger = LoggerFactory.getLogger(UsersRepositoryImpl.class);

  public UsersRepositoryImpl(MongoClient mongoClient) {
    super(mongoClient);
  }

  @Override
  public void createUser(Message<User> message) {
    User user = message.body();
    JsonObject document = JsonObject.mapFrom(user);
    logger.info("saving user: " + document);
    this.mongoClient.save(
      MongoDbCollection.USERS.name,
      document,
      response -> {
        if (response.failed()) {
          logger.error("failed to create user: " + user, response.cause());
          message.fail(500, response.cause().getMessage());
        } else {
          logger.info("successfully created user: " + user);
          message.reply(response.result());
        }
      }
    );
  }

  @Override
  public void findUserByEmail(Message<String> message) {
    String userEmail = message.body();
    logger.info("finding user: " + userEmail);
    this.mongoClient.findOne(
      MongoDbCollection.USERS.name,
      new JsonObject().put("_id", userEmail),
      new JsonObject(),
      response -> {
        if (response.failed()) {
          logger.error("failed to find user " + userEmail + " by email", response.cause());
          message.fail(500, response.cause().getMessage());
        } else {
          JsonObject result = response.result();
          if (result == null) {
            logger.info("user not found: " + userEmail);
            message.reply(null);
          } else {
            logger.info("user found: " + userEmail);
            message.reply(result.mapTo(User.class));
          }
        }
      }
    );
  }
}
