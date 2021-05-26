package com.minesweeper.api.models;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class User implements Serializable {
  String id;
  String name;
}
