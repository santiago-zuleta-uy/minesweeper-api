package com.minesweeper.api.models;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Cell implements Serializable {
  Integer row;
  Integer column;
  Boolean isMined;
  Boolean isFlagged;
  Boolean isRevealed;
}
