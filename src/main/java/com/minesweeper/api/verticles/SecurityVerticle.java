package com.minesweeper.api.verticles;

import com.minesweeper.api.constants.EventBusAddress;
import com.minesweeper.api.services.SecurityService;
import com.minesweeper.api.services.SecurityServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;

public class SecurityVerticle extends AbstractVerticle {

  private SecurityService securityService;

  @Override
  public void start() {
    this.initializeServices();
    this.initializeConsumers();
  }

  private void initializeServices() {
    this.securityService = new SecurityServiceImpl(vertx);
  }

  private void initializeConsumers() {
    this.vertx.eventBus().consumer(
      EventBusAddress.REPOSITORY_AUTHENTICATE_USER.address,
      (Message<String> message) -> this.securityService.authenticate(message)
    );
  }
}
