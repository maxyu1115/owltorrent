package edu.rice.owltorrent.network;

import java.io.IOException;

public interface SelectorHandler {
  void read();

  void write() throws IOException;
}
