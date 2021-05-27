package com.minesweeper.api.constants;

public enum MongoDbCollection {

  GAMES("games"),
  USERS("users");

  public String name;

  MongoDbCollection(String name) {
    this.name = name;
  }
}
