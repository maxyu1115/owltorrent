package edu.rice.owltorrent.common.adapters;

import edu.rice.owltorrent.common.entity.FileBlockInfo;

/** @author Lorraine Lyu, Max Yu */
public interface StorageToNetworkAdapter {
  /**
   * Reports that a block is in progress
   *
   * @param blockInfo block info
   * @return True if should continue with storage, false if already downloaded
   */
  boolean reportBlockInProgress(FileBlockInfo blockInfo);

  /**
   * Reports that a block is completed
   *
   * @param blockInfo block info
   */
  void reportBlockCompletion(FileBlockInfo blockInfo);
}
