package edu.rice.owltorrent.common.adapters;

import edu.rice.owltorrent.common.entity.FilePiece;

/**
 * Adapter for Network package to talk to Storage package.
 *
 * @author Max Yu
 */
public interface NetworkToStorageAdapter {
  FilePiece read();

  void write(FilePiece filePiece);
}
