package com.minesweeper.api.controllers;

import com.minesweeper.api.constants.HttpCode;
import com.minesweeper.api.utils.RoutingContextUtil;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class DocumentationController {

  private final Logger logger = LoggerFactory.getLogger(DocumentationController.class);

  public void getDocumentation(RoutingContext routingContext) {
    routingContext.vertx().fileSystem().readFile("documentation/index.html", response -> {
      if (response.failed()) {
        logger.error("failed to documentation file", response.cause());
        RoutingContextUtil.respondInternalServerError(routingContext);
      } else {
        routingContext.response()
          .putHeader("Content-Type", "text/html")
          .setStatusCode(HttpCode.OK.code)
          .end(response.result());
      }
    });
  }
}
