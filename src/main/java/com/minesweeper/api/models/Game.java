package com.minesweeper.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game implements Serializable {
  String userEmail;
  Integer rows;
  Integer columns;
  Integer mines;
  Map<String, Cell> cells;
  Date startDate;
  GameStatus gameStatus;
}
