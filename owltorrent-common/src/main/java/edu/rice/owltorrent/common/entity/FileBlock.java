package edu.rice.owltorrent.common.entity;

import lombok.Getter;

/**
 * Entity class representing a file block.
 *
 * @author Max Yu
 */
@Getter
public class FileBlock extends FileBlockInfo {
  private final byte[] data;

  public FileBlock(int index, int begin, byte[] data) {
    super(index, begin, data.length);
    this.data = data;
  }
}
