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
    JsonObject document = JsonObject.mapFrom(message.body());
    logger.info("saving user: " + document);
    this.mongoClient.save(
      MongoDbCollection.USERS.name,
      document,
      response -> {
        logger.info("save user response from mongodb: " + response.result());
        if (response.failed()) {
          message.fail(500, response.cause().getMessage());
        }
        message.reply(null);
      }
    );
  }

  @Override
  public void findUserByEmail(Message<String> message) {
    logger.info("finding user: " + message.body());
    this.mongoClient.findOne(
      MongoDbCollection.USERS.name,
      new JsonObject().put("_id", message.body()),
      new JsonObject(),
      response -> {
        if (response.failed()) {
          message.fail(500, response.cause().getMessage());
        } else  {
          JsonObject result = response.result();
          logger.info("find user response from mongodb: " + result);
          message.reply(result.mapTo(User.class));
        }
      }
    );
  }
}
