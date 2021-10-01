package edu.rice.owltorrent.common.adapters;

/** @author Lorraine Lyu, Max Yu */
public interface StorageToNetworkAdapter {
  void startedWritingPiece(int pieceIndex);

  void finishedWriting(int pieceIndex);
}
