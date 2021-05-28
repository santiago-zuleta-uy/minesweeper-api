package com.minesweeper.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cell implements Serializable {
  long row;
  long column;
  boolean isMined;
  Flag flag;
  boolean isRevealed;
  long surroundingMinesCount;

  @Tolerate
  public String key() {
    return row + ":" + column;
  }

  @Tolerate
  public Cell reveal() {
    return this.setRevealed(true);
  }

  public String log() {
    return " | " + getRow() + ":" + getColumn() + " (" + (isMined() ? 1 : 0) + ":" + (isRevealed() ? 1 : 0) + ":" + getSurroundingMinesCount() + ") | ";
  }
}
