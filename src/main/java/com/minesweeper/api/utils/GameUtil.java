package com.minesweeper.api.utils;

import com.minesweeper.api.models.Cell;
import com.minesweeper.api.models.Game;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class GameUtil {

  public Set<Cell> revealCellsRecursively(Game game, Cell cell) {
    cell.reveal();
    Set<Cell> adjacentCells = GameUtil.getAdjacentCells(game, cell);
    long minedCellsFound = adjacentCells.stream().filter(Cell::isMined).count();
    if (minedCellsFound > 0 || adjacentCells.isEmpty()) {
      cell.setSurroundingMinesCount(minedCellsFound);
      return adjacentCells;
    } else {
      return adjacentCells.stream()
        .map(revealedCell -> revealCellsRecursively(game, revealedCell))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    }
  }

  public boolean isGameWon(Game game) {
    if (game.getStatus().isGameOver()) {
      return false;
    } else {
      long totalCells = game.getCells().size();
      long totalMinedCells = game.getMines();
      long totalRevealedOrRedFlaggedCells = game.getCells()
        .values()
        .stream()
        .filter(cell -> cell.isRevealed() || (cell.getFlag() != null && cell.getFlag().isRedFlag()))
        .count();
      boolean isGameWon = totalCells == (totalMinedCells + totalRevealedOrRedFlaggedCells);
      return isGameWon;
    }
  }

  private Set<Cell> getAdjacentCells(Game game, Cell cell) {
    Set<String> adjacentCellsKeys = new HashSet<>();
    for (long row = cell.getRow() - 1; row <= cell.getRow() + 1; row++) {
      for (long column = cell.getColumn() - 1; column <= cell.getColumn() + 1; column++) {
        boolean isNotTargetCell = row != cell.getRow() || column != cell.getColumn();
        if (isNotTargetCell &&
          existsCell(game, row, column) &&
          isNotRevealedCell(game, row, column)
        ) {
          adjacentCellsKeys.add(row + ":" + column);
        }
      }
    }
    return adjacentCellsKeys.stream()
      .map(key -> game.getCells().get(key))
      .collect(Collectors.toSet());
  }

  private boolean isNotRevealedCell(Game game, long row, long column) {
    Cell cell = game.getCells().get(row + ":" + column);
    if (cell == null) {
      return false;
    } else {
      return !cell.isRevealed();
    }
  }

  private boolean existsCell(Game game, long row, long column) {
    return row >= 0 && column >= 0 && row < game.getRows() && column < game.getColumns();
  }
}
