package edu.rice.owltorrent.common.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Represent the FilePiece class.
 *
 * @author yuchengu
 */
@Data
@RequiredArgsConstructor
public class FilePiece {
  private final int pieceIndex;
  private final long offset;
  private final byte[] data;
}
