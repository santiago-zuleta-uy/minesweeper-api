package com.minesweeper.api.services;

import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.models.Game;
import com.minesweeper.api.models.User;
import com.minesweeper.api.utils.RoutingContextUtil;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_CREATE_USER;

public class UserServiceImpl implements UserService {

  private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  private final Vertx vertx;
  private final SecurityService securityService;

  public UserServiceImpl(Vertx vertx) {
    this.vertx = vertx;
    this.securityService = new SecurityServiceImpl(vertx);
  }

  @Override
  public void authenticateUser(RoutingContext routingContext) {
    String email = routingContext.getBodyAsJson().getString("email");
    User user = new User().setEmail(email);
    this.vertx.eventBus().<String>request(REPOSITORY_CREATE_USER.address, user)
      .onSuccess(message -> {
        String token = this.securityService.generateToken(email);
        RoutingContextUtil.respondSuccess(routingContext, new JsonObject().put("token", token));
      })
      .onFailure(throwable -> {
        logger.error("failed to create user " + email, throwable);
        RoutingContextUtil.respondInternalServerError(routingContext);
      });
  }

  @Override
  public void validateUser(RoutingContext routingContext) {
    String token = routingContext.request().getHeader("Authorization");
    this.authenticateUser(token)
      .whenComplete((authenticateUserMessage, authenticateUserThrowable) -> {
        if (authenticateUserThrowable != null) {
          RoutingContextUtil.respondForbidden(routingContext);
        }
        String userEmail = authenticateUserMessage.body().getJsonObject("accessToken").getString("userEmail");
        this.findUserByEmail(userEmail)
          .whenComplete((findUserByEmailMessage, findUserByEmailThrowable) -> {
            User user = findUserByEmailMessage.body();
            if (findUserByEmailThrowable != null || user == null) {
              RoutingContextUtil.respondForbidden(routingContext);
            } else {
              routingContext.put("userEmail", user.getEmail());
              routingContext.next();
            }
          });
      });
  }

  @Override
  public void validateUserGame(RoutingContext routingContext) {
    String gameId = routingContext.pathParam("gameId");
    String userEmail = routingContext.get("userEmail");
    this.findUserGamesByEmail(userEmail)
      .whenComplete((findUserGamesByEmailMessage, findUserGamesByEmailThrowable) -> {
        if (findUserGamesByEmailThrowable != null) {
          RoutingContextUtil.respondForbidden(routingContext);
        }
        boolean existsGame = findUserGamesByEmailMessage.body()
          .stream()
          .anyMatch(game -> game.getId().equals(gameId));
        if (existsGame) {
          routingContext.next();
        } else {
          RoutingContextUtil.respondForbidden(routingContext);
        }
      });
  }

  private CompletionStage<Message<JsonObject>> authenticateUser(String token) {
    return this.vertx.eventBus()
      .<JsonObject>request(EventBusAddress.REPOSITORY_AUTHENTICATE_USER.address, token)
      .onFailure(throwable -> logger.error("failed to authenticate user with token " + token, throwable))
      .toCompletionStage();
  }

  private CompletionStage<Message<List<Game>>> findUserGamesByEmail(String userEmail) {
    return this.vertx.eventBus()
      .<List<Game>>request(EventBusAddress.REPOSITORY_FIND_GAMES_BY_USER_EMAIL.address, userEmail)
      .onFailure(throwable -> logger.error("failed to find user games for " + userEmail, throwable))
      .toCompletionStage();
  }

  private CompletionStage<Message<User>> findUserByEmail(String userEmail) {
    return this.vertx.eventBus()
      .<User>request(EventBusAddress.REPOSITORY_FIND_USER_BY_EMAIL.address, userEmail)
      .onFailure(throwable -> logger.error("failed to find user  " + userEmail, throwable))
      .toCompletionStage();
  }
}
