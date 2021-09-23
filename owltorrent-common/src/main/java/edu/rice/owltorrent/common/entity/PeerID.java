package edu.rice.owltorrent.common.entity;

import lombok.Getter;

// TODO: finish this
public class PeerID {
  @Getter String id;

  public byte[] getBytes() {
    // TODO: replace this with actual value.
    return new byte[20];
  }
}
