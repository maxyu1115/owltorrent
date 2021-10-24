package edu.rice.owltorrent.common.entity;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represent the Torrent class after parsing.
 *
 * @author yuchengu
 */
@Data
@AllArgsConstructor
public class Torrent {

  /** The URL of the tracker. */
  private String announceURL;

  /** A UTF-8 encoded string which is the suggested name to save the file (or directory) as. */
  private String name;

  /** The number of bytes in each piece the file is split into */
  private long pieceLength;

  // TODO: not the most efficient, consider refactoring.
  /** @return the length of the last piece */
  public long getLastPieceLength() {
    long totalLength = getTotalLength();
    return totalLength % pieceLength == 0 ? pieceLength : totalLength % pieceLength;
  }

  public long getTotalLength() {
    long totalLength = 0;
    for (var entry : fileLengths.entrySet()) {
      totalLength += entry.getValue();
    }
    return totalLength;
  }

  /**
   * A list of byte array whose length is a multiple of 20. It is to be subdivided into strings of
   * length 20, each of which is the SHA1 hash of the piece at the corresponding index.
   */
  private List<byte[]> pieces;

  /** The lengths of each file in Torrent. */
  private Map<String, Long> fileLengths;

  private TwentyByteId infoHash;
  //  private String infoHash;

  public Torrent() {}
}
