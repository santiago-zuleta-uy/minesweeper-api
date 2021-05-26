package com.minesweeper.api.models;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class ModelConverter {
  public static Buffer toBuffer(JsonObject data) {
    return Json.encodeToBuffer(data);
  }
}
