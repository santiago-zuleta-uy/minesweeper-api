package com.minesweeper.api.models;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Cell implements Serializable {
  int row;
  int column;
  boolean isMined;
  boolean isFlagged;
  boolean isRevealed;
}
