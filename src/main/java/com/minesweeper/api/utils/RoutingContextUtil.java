package com.minesweeper.api.utils;

import com.minesweeper.api.constants.HttpCode;
import com.minesweeper.api.verticles.AbstractWebServerVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RoutingContextUtil {

  private final Logger logger = LoggerFactory.getLogger(AbstractWebServerVerticle.class);

  public void respondSuccess(RoutingContext routingContext, JsonObject body) {
    respond(routingContext, HttpCode.OK.code, body);
  }

  public void respondSuccess(RoutingContext routingContext) {
    respond(routingContext, HttpCode.OK.code, null);
  }

  public void respondCreated(RoutingContext routingContext, JsonObject body) {
    respond(routingContext, HttpCode.CREATED.code, body);
  }

  public void respondCreated(RoutingContext routingContext) {
    respond(routingContext, HttpCode.CREATED.code, null);
  }

  public void respondBadRequest(RoutingContext routingContext) {
    respondError(routingContext, HttpCode.BAD_REQUEST.code, null);
  }

  public void respondBadRequest(RoutingContext routingContext, String message) {
    respondError(routingContext, HttpCode.BAD_REQUEST.code, message);
  }

  public void respondBadRequestIfNullPathParam(RoutingContext routingContext, String paramKey) {
    String paramValue = routingContext.pathParam(paramKey);
    if (paramValue == null) {
      respondError(routingContext, HttpCode.BAD_REQUEST.code, "Missing path parameter " + paramKey);
    } else {
      routingContext.next();
    }
  }

  public void respondBadRequestIfNullQueryParam(RoutingContext routingContext, String paramKey) {
    String paramValue = routingContext.queryParams().get(paramKey);
    if (paramValue == null) {
      respondError(routingContext, HttpCode.BAD_REQUEST.code, "Missing query parameter " + paramKey);
    } else {
      routingContext.next();
    }
  }

  public void respondBadRequestIfNullBodyParam(RoutingContext routingContext, String paramKey) {
    String paramValue = routingContext.getBodyAsJson().getString(paramKey);
    if (paramValue == null) {
      respondError(routingContext, HttpCode.BAD_REQUEST.code, "Missing body parameter " + paramKey);
    } else {
      routingContext.next();
    }
  }

  public void respondForbidden(RoutingContext routingContext) {
    respond(routingContext, HttpCode.FORBIDDEN.code, null);
  }

  public void respondNotFound(RoutingContext routingContext) {
    respondError(routingContext, HttpCode.NOT_FOUND.code, null);
  }

  public void respondBadGateway(RoutingContext routingContext) {
    respondError(routingContext, HttpCode.BAD_GATEWAY.code, null);
  }

  public void respondInternalServerError(RoutingContext routingContext) {
    respondError(routingContext, HttpCode.INTERNAL_SERVER_ERROR.code, null);
  }

  public void respondError(RoutingContext context, int statusCode, String message) {
    String logMessage = message + " " + getRequestAndResponseContext(context, statusCode, message);
    logger.error(logMessage, context.failure());
    if (message != null) {
      JsonObject response = new JsonObject()
        .put("status", statusCode)
        .put("message", message);
      respond(context, statusCode, response);
    } else {
      respond(context, statusCode, null);
    }
  }

  public void respond(RoutingContext context, int statusCode, JsonObject responseBody) {
    if (responseBody == null) {
      buildResponse(context, statusCode).end();
    } else {
      Buffer bufferedResponseBody = ModelConverter.toBuffer(responseBody);
      buildResponse(context, statusCode).end(bufferedResponseBody);
    }
  }

  public HttpServerResponse buildResponse(RoutingContext context, int statusCode) {
    return context.response()
      .putHeader("Content-Type", "application-json")
      .setStatusCode(statusCode);
  }

  public String getRequestAndResponseContext(
    RoutingContext routingContext,
    int responseStatus,
    String message
  ) {
    HttpServerRequest request = routingContext.request();
    return String.format(
      "[requestPath: %s, requestQuery: %s, responseStatus: %d, message: %s]",
      request.path(),
      request.query(),
      responseStatus,
      message
    );
  }
}
