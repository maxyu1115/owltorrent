package edu.rice.owltorrent.core.entity;

import lombok.Data;

/** Represent the Torrent class after parsing. */
@Data
public class Torrent {
  // FIXME: temporary for hello world
  private String peerIPAddress;

  /** The URL of the tracker. */
  private String announceURL;

  /** A UTF-8 encoded string which is the suggested name to save the file (or directory) as. */
  private String name;

  /** The number of bytes in each piece the file is split into */
  private int pieceLength;

  /**
   * A string whose length is a multiple of 20. It is to be subdivided into strings of length 20,
   * each of which is the SHA1 hash of the piece at the corresponding index.
   */
  private byte[] pieces;

  /** The length of the file, in bytes. */
  private int length;

  // FIXME: temporary for hello world
  public Torrent(String peerIPAddress) {
    this.peerIPAddress = peerIPAddress;
  }

  // TODO: Add constructor
  public Torrent() {}

  public String getAnnounceURL() {
    return announceURL;
  }

  public void setAnnounceURL(String announceURL) {
    this.announceURL = announceURL;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getPieceLength() {
    return pieceLength;
  }

  public void setPieceLength(int pieceLength) {
    this.pieceLength = pieceLength;
  }

  public byte[] getPieces() {
    return pieces;
  }

  public void setPieces(byte[] pieces) {
    this.pieces = pieces;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }
}
