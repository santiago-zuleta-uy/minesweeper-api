package com.minesweeper.api.models;

public enum GameStatus {
  GAME_IN_PROGRESS, GAME_PAUSED, GAME_OVER, GAME_WON;

  public boolean isGameOver() {
    return this == GAME_OVER;
  }

  public boolean isGameWon() {
    return this == GAME_WON;
  }

  public boolean isGamePaused() {
    return this == GAME_PAUSED;
  }

  public boolean isGameInProgress() {
    return this == GAME_IN_PROGRESS;
  }

  public boolean isNotGameInProgress() {
    return this != GAME_IN_PROGRESS;
  }
}
