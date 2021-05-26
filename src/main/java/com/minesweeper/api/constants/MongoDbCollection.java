package com.minesweeper.api.constants;

public enum MongoDbCollection {

  GAMES("games");

  public String name;

  MongoDbCollection(String name) {
    this.name = name;
  }
}
