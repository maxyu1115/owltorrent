package edu.rice.owltorrent.common.adapters;

import edu.rice.owltorrent.common.entity.FilePiece;
import edu.rice.owltorrent.common.util.Exceptions;
import java.io.IOException;

/**
 * Adapter to talk to the Storage package.
 *
 * @author Max Yu
 */
public interface StorageAdapter {

  FilePiece read(int pieceIndex, int pieceOffset, int length)
      throws Exceptions.IllegalByteOffsets, IOException;

  void write(FilePiece filePiece) throws Exceptions.IllegalByteOffsets, IOException;
}
