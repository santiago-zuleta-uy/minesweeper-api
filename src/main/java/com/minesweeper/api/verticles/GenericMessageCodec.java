package com.minesweeper.api.verticles;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class GenericMessageCodec<T> implements MessageCodec<T, T> {

  private final Class<T> clazz;

  public GenericMessageCodec(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public void encodeToWire(Buffer buffer, T t) {
  }

  @Override
  public T decodeFromWire(int pos, Buffer buffer) {
    return null;
  }

  @Override
  public T transform(T t) {
    return t;
  }

  @Override
  public String name() {
    return clazz.getName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
