package com.minesweeper.api.models;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Game implements Serializable {
  String id;
  String userId;
  Integer rows;
  Integer columns;
  Integer mines;
  Map<String, Cell> cells;
  Date startDate;
}
