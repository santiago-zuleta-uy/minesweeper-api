package com.minesweeper.api.verticles;

import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.models.Game;
import com.minesweeper.api.respositories.GamesRepository;
import com.minesweeper.api.respositories.GamesRepositoryImpl;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.io.IOException;

public class EmbeddedMongoVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(EmbeddedMongoVerticle.class);

  private MongodExecutable mongodExecutable;
  private MongoClient mongoClient;
  private GamesRepository gamesRepository;

  @Override
  public void start() throws Exception {
    initializeEmbeddedMongoDb();
    initializeRepositories();
  }

  private void initializeEmbeddedMongoDb() throws IOException {
    logger.info("initializing embedded mongo db");
    MongodStarter mongodStarter = MongodStarter.getDefaultInstance();
    int port = Network.getFreeServerPort();
    MongodConfig mongodConfig = MongodConfig.builder()
      .version(Version.Main.PRODUCTION)
      .net(new Net(port, Network.localhostIsIPv6()))
      .build();
    mongodExecutable = mongodStarter.prepare(mongodConfig);
    mongodExecutable.start();
    JsonObject config = new JsonObject()
      .put("host", "localhost")
      .put("port", port)
      .put("db_name", "minesweeper");
    mongoClient = MongoClient.createShared(vertx, config);
    mongoClient.createCollection("games");
    logger.info("embedded mongo db started and listening at port " + port);
  }

  private void initializeRepositories() {
    this.gamesRepository = new GamesRepositoryImpl(mongoClient);
    vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_CREATE_GAME.address,
      (Message<Game> message) -> this.gamesRepository.createGame(message)
    );
    vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_FIND_GAME_BY_ID.address,
      (Message<String> message) -> this.gamesRepository.findGameById(message)
    );
  }

  @Override
  public void stop() {
    mongodExecutable.stop();
  }
}
