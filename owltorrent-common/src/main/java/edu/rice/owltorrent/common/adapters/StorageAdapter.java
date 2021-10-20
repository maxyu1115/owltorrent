package edu.rice.owltorrent.common.adapters;

import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.util.Exceptions;
import java.io.IOException;

/**
 * Adapter for Network package to talk to Storage package.
 *
 * @author Max Yu
 */
public interface StorageAdapter {
  FileBlock read(FileBlockInfo fileBlockInfo) throws Exceptions.IllegalByteOffsets, IOException;
  ;

  void write(FileBlock fileBlock) throws Exceptions.IllegalByteOffsets, IOException;

  boolean verify(int pieceIndex, byte[] sha1Hash) throws Exceptions.IllegalByteOffsets, IOException;
}
