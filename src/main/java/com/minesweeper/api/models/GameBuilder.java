package com.minesweeper.api.models;

import java.util.*;
import java.util.stream.Collectors;

public class GameBuilder {

  private Game game;

  private GameBuilder() {
    this.game = new Game();
  }

  public GameBuilder withRows(Integer rows) {
    this.game.setRows(rows);
    return this;
  }

  public GameBuilder withColumns(Integer columns) {
    this.game.setColumns(columns);
    return this;
  }

  public GameBuilder withMines(Integer mines) {
    this.game.setMines(mines);
    return this;
  }

  public GameBuilder withUserId(String userId) {
    this.game.setUserId(userId);
    return this;
  }

  public Game build() {
    List<Cell> cells = new ArrayList<>();
    for (int row = 0; row < this.game.getRows(); row++) {
      for (int column = 0; column < this.game.getColumns(); column++) {
        cells.add(
          new Cell()
            .setRow(row)
            .setColumn(column)
            .setIsFlagged(false)
            .setIsRevealed(false)
            .setIsMined(false)
        );
      }
    }
    Collections.shuffle(cells);
    cells.stream().limit(this.game.getMines()).forEach(cell -> cell.setIsMined(true));
    Map<String, Cell> cellsMap = cells.stream()
      .collect(
        Collectors.toMap(
          cell -> cell.getRow().toString() + cell.getColumn().toString(),
          cell -> cell
        )
      );
    return this.game
      .setCells(cellsMap)
      .setStartDate(new Date());
  }

  public static GameBuilder get() {
    return new GameBuilder();
  }
}
