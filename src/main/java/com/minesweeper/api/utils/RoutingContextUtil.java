package com.minesweeper.api.utils;

import com.minesweeper.api.constants.HttpCode;
import com.minesweeper.api.models.ModelConverter;
import com.minesweeper.api.verticles.AbstractWebServerVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class RoutingContextUtil {

  private static final Logger logger = LoggerFactory.getLogger(AbstractWebServerVerticle.class);

  public static void respondSuccess(RoutingContext routingContext, JsonObject body) {
    respond(routingContext, HttpCode.OK.code, body);
  }

  public static void respondCreated(RoutingContext routingContext, JsonObject body) {
    respond(routingContext, HttpCode.OK.code, body);
  }

  public static void respondBadRequest(RoutingContext routingContext) {
    respondError(routingContext, HttpCode.BAD_REQUEST.code, "Bad request");
  }

  public static void respondForbidden(RoutingContext routingContext) {
    respondError(routingContext, HttpCode.FORBIDDEN.code, "Forbidden");
  }

  public static void respondNotFound(RoutingContext routingContext) {
    respondError(routingContext, HttpCode.NOT_FOUND.code, "Not found");
  }

  public static void respondBadGateway(RoutingContext routingContext) {
    respondError(routingContext, HttpCode.BAD_GATEWAY.code, "Bad gateway");
  }

  public static void respondInternalServerError(RoutingContext routingContext) {
    respondError(routingContext, HttpCode.INTERNAL_SERVER_ERROR.code, "Internal server error");
  }

  public static void respondError(RoutingContext context, int statusCode, String message) {
    String responseMsg = context.failure().getMessage();
    JsonObject response = new JsonObject()
      .put("status", statusCode)
      .put("message", responseMsg);
    String logMessage = message + " " + getRequestAndResponseContext(context, statusCode, response.toString());
    logger.error(logMessage, context.failure());
    respond(context, statusCode, response);
  }

  public static void respond(RoutingContext context, int statusCode, JsonObject responseBody) {
    Buffer bufferedResponseBody = ModelConverter.toBuffer(responseBody);
    buildResponse(context, statusCode).end(bufferedResponseBody);
  }

  public static HttpServerResponse buildResponse(RoutingContext context, int statusCode) {
    return context.response()
      .putHeader("Content-Type", "application-json")
      .setStatusCode(statusCode);
  }

  public static String getRequestAndResponseContext(
    RoutingContext routingContext,
    int responseStatus,
    String responseBody
  ) {
    HttpServerRequest request = routingContext.request();
    return String.format(
      "[requestPath: %s, requestQuery: %s, responseStatus: %d, responseBody: %s]",
      request.path(),
      request.query(),
      responseStatus,
      responseBody
    );
  }
}
