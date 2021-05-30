package com.minesweeper.api.models;

import java.util.Arrays;

public enum CellFlag {
  QUESTION_MARK,
  RED_FLAG;

  public static CellFlag get(String flagName) {
    return Arrays.stream(values())
      .filter(cellFlag -> cellFlag.name().equals(flagName))
      .findFirst()
      .orElse(null);
  }

  public boolean isRedFlag() {
    return this == RED_FLAG;
  }
}
