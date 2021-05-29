package com.minesweeper.api.services;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

import java.time.Duration;
import java.time.OffsetDateTime;

public class SecurityServiceImpl implements SecurityService {

  private static Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

  private final JWTAuth jwtAuth;

  public SecurityServiceImpl(Vertx vertx) {
    PubSecKeyOptions pubSecKeyOptions = new PubSecKeyOptions()
      .setAlgorithm("HS256")
      .setBuffer("The Secret");
    JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
      .addPubSecKey(pubSecKeyOptions);
    this.jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);
  }

  @Override
  public String generateToken(String userEmail) {
    long expirationTime = OffsetDateTime.now().plus(Duration.ofDays(1)).toEpochSecond();
    return this.jwtAuth.generateToken(
      new JsonObject().put("userEmail", userEmail).put("exp", expirationTime)
    );
  }

  @Override
  public void authenticate(Message<String> message) {
    JsonObject credentials = new JsonObject().put("token", message.body());
    this.jwtAuth.authenticate(credentials).onSuccess(response -> {
      if (response.expired()) {
        message.fail(500, "authorization token expired");
      } else {
        message.reply(response.attributes());
      }
    }).onFailure(throwable -> {
      logger.error("failed to authenticate token " + message, throwable);
      message.fail(500, throwable.getMessage());
    });
  }
}
