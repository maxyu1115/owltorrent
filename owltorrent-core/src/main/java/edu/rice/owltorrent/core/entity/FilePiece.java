package edu.rice.owltorrent.core.entity;

import lombok.Data;

/** Represent the FilePiece class. */
@Data
public class FilePiece {
  private final int index;
  private final long length;
  private final byte[] hash;

  /**
   * Initialize a piece
   *
   * @param index The piece index in the torrent.
   * @param length The piece's length,in bytes
   * @param hash The piece's 20-byte SHA1 hash.
   */
  public FilePiece(int index, long length, byte[] hash) {
    this.index = index;
    this.length = length;
    this.hash = hash;
  }
}
