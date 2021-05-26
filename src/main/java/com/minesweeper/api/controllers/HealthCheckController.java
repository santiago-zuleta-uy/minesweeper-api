package com.minesweeper.api.controllers;

import com.minesweeper.api.utils.RoutingContextUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class HealthCheckController {

  private final JsonObject response = new JsonObject().put("status", "ok");

  public void health(RoutingContext routingContext) {
    RoutingContextUtil.respondSuccess(routingContext, this.response);
  }
}
