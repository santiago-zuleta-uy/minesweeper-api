package com.minesweeper.api.models;

import java.util.Arrays;

public enum CellFlag {
  QUESTION_MARK,
  RED_FLAG;

  public static CellFlag get(String flagName) {
    return Arrays.stream(values())
      .filter(f -> f.name().equals(flagName))
      .findFirst()
      .orElse(null);
  }
}
