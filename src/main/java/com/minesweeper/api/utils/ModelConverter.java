package com.minesweeper.api.utils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ModelConverter {
  public Buffer toBuffer(JsonObject data) {
    return Json.encodeToBuffer(data);
  }
}
