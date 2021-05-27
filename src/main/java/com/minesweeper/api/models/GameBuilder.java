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

  public GameBuilder withUserEmail(String userEmail) {
    this.game.setUserEmail(userEmail);
    return this;
  }

  private List<Cell> getCellsShuffledAndMined() {
    List<Cell> cells = new ArrayList<>();
    for (int row = 0; row < this.game.getRows(); row++) {
      for (int column = 0; column < this.game.getColumns(); column++) {
        cells.add(
          new Cell()
            .setRow(row)
            .setColumn(column)
            .setFlagged(false)
            .setRevealed(false)
            .setMined(false)
        );
      }
    }
    Collections.shuffle(cells);
    cells.stream()
      .limit(this.game.getMines())
      .forEach(cell -> cell.setMined(true));
    return cells;
  }

  /**
   * Maps cells by row and column in order to improve efficiency when searching for a particular cell.
   * Key format pattern is "{row}:{column}", example "15:5" row 15 column 5.
   *
   * @param cells
   * @return Cells mapped by row and column
   */
  private Map<String, Cell> mapCellsByRowAndColumn(List<Cell> cells) {
    return cells.stream()
      .collect(
        Collectors.toMap(
          cell -> cell.getRow() + ":" + cell.getColumn(),
          cell -> cell
        )
      );
  }

  public Game build() {
    List<Cell> cells = this.getCellsShuffledAndMined();
    Map<String, Cell> cellsMap = this.mapCellsByRowAndColumn(cells);
    return this.game
      .setCells(cellsMap)
      .setStartDate(new Date());
  }

  public static GameBuilder get() {
    return new GameBuilder();
  }
}
