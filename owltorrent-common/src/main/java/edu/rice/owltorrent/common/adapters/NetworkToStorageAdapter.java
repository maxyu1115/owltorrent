package edu.rice.owltorrent.common.adapters;

import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.entity.Torrent;

/**
 * Adapter for Network package to talk to Storage package.
 *
 * @author Max Yu
 */
public interface NetworkToStorageAdapter {
  FileBlock read(FileBlockInfo fileBlockInfo);

  void write(Torrent torrent, FileBlock fileBlock);
}
