package com.minesweeper.api.builders;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoutingContextMockBuilder {

  private final RoutingContext routingContext;

  private RoutingContextMockBuilder(RoutingContext routingContext) {
    this.routingContext = routingContext;
  }

  public RoutingContextMockBuilder withBodyParameter(JsonObject body) {
    when(routingContext.getBodyAsJson()).thenReturn(body);
    return this;
  }

  public RoutingContextMockBuilder withUserEmailContextValue(String userEmail) {
    when(routingContext.get(eq("userEmail"))).thenReturn(userEmail);
    return this;
  }

  public RoutingContextMockBuilder withGameIdPathParameter(String gameId) {
    when(routingContext.get(eq("gameId"))).thenReturn(gameId);
    return this;
  }

  public RoutingContext build() {
    HttpServerResponse httpServerResponse = mock(HttpServerResponse.class);
    when(httpServerResponse.putHeader(any(String.class), any(String.class))).thenReturn(httpServerResponse);
    when(httpServerResponse.setStatusCode(any(int.class))).thenReturn(httpServerResponse);
    when(httpServerResponse.end(any(Buffer.class))).thenReturn(null);
    when(httpServerResponse.end()).thenReturn(null);
    when(routingContext.response()).thenReturn(httpServerResponse);
    return routingContext;
  }

  public static RoutingContextMockBuilder of() {
    return new RoutingContextMockBuilder(mock(RoutingContext.class));
  }
}
