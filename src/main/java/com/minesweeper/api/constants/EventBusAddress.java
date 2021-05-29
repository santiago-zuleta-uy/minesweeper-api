package com.minesweeper.api.constants;

public enum EventBusAddress {

  REPOSITORY_UPSERT_GAME("repository::upsert::game"),
  REPOSITORY_FIND_GAME_BY_ID("repository::find::game_by_id"),
  REPOSITORY_FIND_GAMES_BY_USER_EMAIL("repository::find::games_by_user_email"),
  REPOSITORY_CREATE_USER("repository::create::user"),
  REPOSITORY_FIND_USER_BY_EMAIL("repository::find::user_by_email"),
  REPOSITORY_AUTHENTICATE_USER("repository::authenticate::user");

  public String address;

  EventBusAddress(String address) {
    this.address = address;
  }
}
