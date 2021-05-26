package com.minesweeper.api.constants;

public enum EventBusAddress {

  REPOSITORY_CREATE_GAME("repository::create::game"),
  REPOSITORY_FIND_GAME_BY_ID("repository::find::game_by_id");

  public String address;

  EventBusAddress(String address) {
    this.address = address;
  }
}
