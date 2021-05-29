package com.minesweeper.api.verticles;

import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.constants.MongoDbCollection;
import com.minesweeper.api.models.Game;
import com.minesweeper.api.models.User;
import com.minesweeper.api.respositories.GamesRepository;
import com.minesweeper.api.respositories.GamesRepositoryImpl;
import com.minesweeper.api.respositories.UsersRepository;
import com.minesweeper.api.respositories.UsersRepositoryImpl;
import de.flapdoodle.embed.mongo.MongodExecutable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.io.IOException;

import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_FIND_GAMES_BY_USER_EMAIL;

public class EmbeddedMongoVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(EmbeddedMongoVerticle.class);

  private MongodExecutable mongodExecutable;
  private MongoClient mongoClient;
  private GamesRepository gamesRepository;
  private UsersRepository usersRepository;

  @Override
  public void start() throws Exception {
    initializeEmbeddedMongoDb();
    initializeRepositories();
  }

  private void initializeEmbeddedMongoDb() throws IOException {
    logger.info("initializing embedded mongo db");
//    MongodStarter mongodStarter = MongodStarter.getDefaultInstance();
    int port = 27017;//Network.getFreeServerPort();
//    MongodConfig mongodConfig = MongodConfig.builder()
//      .version(Version.Main.PRODUCTION)
//      .net(new Net(port, Network.localhostIsIPv6()))
//      .build();
//    mongodExecutable = mongodStarter.prepare(mongodConfig);
//    mongodExecutable.start();
    JsonObject config = new JsonObject()
      .put("host", "localhost")
      .put("port", port)
      .put("db_name", "minesweeper");
    mongoClient = MongoClient.createShared(vertx, config);
    mongoClient.createCollection(MongoDbCollection.GAMES.name);
    mongoClient.createCollection(MongoDbCollection.USERS.name);
    mongoClient.createIndex(
      MongoDbCollection.GAMES.name,
      new JsonObject().put("userEmail", 1),
      response -> {
        if (response.failed()) {
          logger.error("failed to create userEmail index", response.cause());
        }
      }
    );
    logger.info("embedded mongo db initialized and listening at port " + port);
  }

  private void initializeRepositories() {
    this.gamesRepository = new GamesRepositoryImpl(mongoClient);
    this.usersRepository = new UsersRepositoryImpl(mongoClient);
    this.vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_UPSERT_GAME.address,
      (Message<Game> message) -> this.gamesRepository.updateGame(message)
    );
    this.vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_FIND_GAME_BY_ID.address,
      (Message<String> message) -> this.gamesRepository.findGameById(message)
    );
    this.vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_CREATE_USER.address,
      (Message<User> message) -> this.usersRepository.createUser(message)
    );
    this.vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_FIND_USER_BY_EMAIL.address,
      (Message<String> message) -> this.usersRepository.findUserByEmail(message)
    );
    this.vertx.eventBus().consumer(
      REPOSITORY_FIND_GAMES_BY_USER_EMAIL.address,
      (Message<String> message) -> this.gamesRepository.findGameByUserEmail(message)
    );
  }

  @Override
  public void stop() {
    mongodExecutable.stop();
  }
}
