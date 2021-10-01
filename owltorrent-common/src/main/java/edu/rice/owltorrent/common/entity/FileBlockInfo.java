package edu.rice.owltorrent.common.entity;

import lombok.Data;

/**
 * Info of a file block. A piece consists of at least two file blocks. : (
 *
 * @author Max Yu
 */
@Data
public class FileBlockInfo {
  protected final int index;
  protected final int begin;
  protected final long length;
}
