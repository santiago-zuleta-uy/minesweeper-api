package com.minesweeper.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game implements Serializable {
  @JsonProperty("_id")
  String id;
  String userEmail;
  int rows;
  int columns;
  int mines;
  Map<String, Cell> cells;
  Date startDateMillis;
  Date resumeDateMillis;
  long secondsPlayed;
  GameStatus status;

  @Tolerate
  public void putCell(Cell cell) {
    this.getCells().put(cell.key(), cell);
  }

  @Tolerate
  public void putCells(Collection<Cell> cells) {
    this.getCells().putAll(
      cells.stream()
        .collect(Collectors.toMap(
          Cell::key, cell -> cell, (cell1, cell2) -> cell2
        ))
    );
  }

  @Tolerate
  public void updateSecondsPlayed() {
    if (this.resumeDateMillis == null) {
      long millisElapsed = new Date().getTime() - this.startDateMillis.getTime();
      long totalSecondsFromStartDate = Duration.ofMillis(millisElapsed).getSeconds();
      this.setSecondsPlayed(totalSecondsFromStartDate);
    } else {
      long millisElapsed = new Date().getTime() - this.resumeDateMillis.getTime();
      long totalSecondsFromResumeDate = Duration.ofMillis(millisElapsed).getSeconds();
      this.setSecondsPlayed(this.secondsPlayed - totalSecondsFromResumeDate);
    }
  }

  @Tolerate
  public void resumeIfPaused() {
    if (GameStatus.PAUSED == this.getStatus()) {
      this.setResumeDateMillis(new Date());
      this.setStatus(GameStatus.IN_PROGRESS);
      this.updateSecondsPlayed();
    }
  }
}
