package edu.rice.owltorrent.network;

import lombok.Getter;

public enum Event {
  NONE(0),
  COMPLETED(1),
  STARTED(2),
  STOPPED(3);

  Event(int code) {
    this.eventCode = code;
  }

  @Getter private final int eventCode;
}
