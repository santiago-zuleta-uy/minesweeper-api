package com.minesweeper.api.verticles;

import com.minesweeper.api.constants.HttpCode;
import com.minesweeper.api.utils.RoutingContextUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.StaticHandler;

public abstract class AbstractWebServerVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(AbstractWebServerVerticle.class);

  private HttpServer httpServer;
  private Router router;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    this.httpServer = vertx.createHttpServer();
    this.router = Router.router(this.vertx);
  }

  @Override
  public void start() {
    logger.info("starting web server");
    addRoutes();
    listen();
    logger.info("web server started and listening at port 8080");
  }

  private void listen() {
    this.httpServer.requestHandler(this.router);
    this.httpServer.listen(8080);
  }

  private void addRoutes() {
    handleGlobalDefaults();
    handleErrors();
    routes(this.router);
  }

  private void handleGlobalDefaults() {
    String catchAllRoute = "/*";
    String jsonMimeType = "application/json";
    this.router.post(catchAllRoute).handler(BodyHandler.create());
    this.router.patch(catchAllRoute).handler(BodyHandler.create());
    this.router.route().handler(FaviconHandler.create(vertx));
    this.router.route(catchAllRoute)
      .consumes(jsonMimeType)
      .produces(jsonMimeType)
      .handler(ResponseContentTypeHandler.create());
  }

  private void handleErrors() {
    this.router.errorHandler(HttpCode.BAD_REQUEST.code, RoutingContextUtil::respondBadRequest);
    this.router.errorHandler(HttpCode.INTERNAL_SERVER_ERROR.code, RoutingContextUtil::respondInternalServerError);
    this.router.errorHandler(HttpCode.FORBIDDEN.code, RoutingContextUtil::respondForbidden);
    this.router.errorHandler(HttpCode.NOT_FOUND.code, RoutingContextUtil::respondNotFound);
    this.router.errorHandler(HttpCode.BAD_GATEWAY.code, RoutingContextUtil::respondBadGateway);
  }

  protected abstract void routes(Router router);
}
