package com.minesweeper.api.constants;

public enum EventBusAddress {

  REPOSITORY_UPSERT_GAME("repository::upsert::game"),
  REPOSITORY_FIND_GAME_BY_ID("repository::find::game_by_id"),
  REPOSITORY_CREATE_USER("repository::create::user"),
  REPOSITORY_FIND_USER_BY_EMAIL("repository::find::user_by_email");

  public String address;

  EventBusAddress(String address) {
    this.address = address;
  }
}
