package com.minesweeper.api.services;

import io.vertx.core.eventbus.Message;

public interface SecurityService {
  String generateToken(String userEmail);
  void authenticate(Message<String> message);
}
