package edu.rice.owltorrent.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

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

  /**
   * A string whose length is a multiple of 20. It is to be subdivided into strings of length 20,
   * each of which is the SHA1 hash of the piece at the corresponding index.
   */
  private List<String> pieces;

  /** The lengths of each file in Torrent. */
  private HashMap<String, Long> fileLengths;

  private TwentyByteId infoHash;
//  private String infoHash;

  public Torrent() {}
}
