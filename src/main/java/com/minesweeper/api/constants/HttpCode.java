package com.minesweeper.api.constants;

public enum HttpCode {
  OK(200),
  CREATED(201),
  BAD_REQUEST(400),
  NOT_FOUND(404),
  INTERNAL_SERVER_ERROR(500),
  FORBIDDEN(403),
  BAD_GATEWAY(503);

  public int code;

  HttpCode(int code) {
    this.code = code;
  }
}
