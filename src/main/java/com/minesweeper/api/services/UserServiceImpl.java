package com.minesweeper.api.services;

import com.minesweeper.api.models.User;
import com.minesweeper.api.utils.RoutingContextUtil;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import static com.minesweeper.api.constants.EventBusAddress.REPOSITORY_CREATE_USER;

public class UserServiceImpl implements UserService {

  private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  private final Vertx vertx;

  public UserServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void createUser(RoutingContext routingContext) {
    String email = routingContext.getBodyAsJson().getString("email");
    User user = new User().setEmail(email);
    this.vertx.eventBus().<String>request(REPOSITORY_CREATE_USER.address, user).onSuccess(message -> {
      RoutingContextUtil.respondCreated(routingContext);
    });
  }
}
