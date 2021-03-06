package edu.rice.owltorrent.common.entity;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Thread safe entity class for remote Peers.
 *
 * @author Lorraine Lyu, Max Yu
 */
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Peer {
  @Getter private TwentyByteId peerID;
  /** NOTE: address may be null!!! (for peers that connected to us) */
  @EqualsAndHashCode.Include @Getter private final InetSocketAddress address;

  @EqualsAndHashCode.Include @Getter private final Torrent torrent;

  @Getter @ToString.Exclude private Bitfield bitfield = Bitfield.EMPTY_BITFIELD_SINGLETON;

  // TODO: add actual interested/choking algorithm
  /** this client is interested in the peer */
  private final AtomicBoolean amInterested = new AtomicBoolean(true);
  /** this client is choking the peer */
  private final AtomicBoolean amChoked = new AtomicBoolean(false);
  /** peer is interested in this client */
  private final AtomicBoolean peerInterested = new AtomicBoolean(false);
  /** peer is choking this client */
  private final AtomicBoolean peerChoked = new AtomicBoolean(true);

  public Peer(TwentyByteId peerID, InetSocketAddress address, Torrent torrent) {
    this.peerID = peerID;
    this.address = address;
    this.torrent = torrent;
  }

  public boolean isAmInterested() {
    return this.amInterested.get();
  }

  public boolean isPeerInterested() {
    return this.peerInterested.get();
  }

  public boolean isAmChoked() {
    return this.amChoked.get();
  }

  public boolean isPeerChoked() {
    return this.peerChoked.get();
  }

  public void setPeerID(TwentyByteId peerID) {
    if (this.peerID != null) {
      throw new IllegalStateException("This Peer's ID is already set");
    }
    this.peerID = peerID;
  }

  public void setAmInterested(boolean amInterested) {
    this.amInterested.set(amInterested);
  }

  public void setPeerInterested(boolean peerInterested) {
    this.peerInterested.set(peerInterested);
  }

  public void setAmChoked(boolean choked) {
    this.amChoked.set(choked);
  }

  public void setPeerChoked(boolean choked) {
    this.peerChoked.set(choked);
  }

  /**
   * Returns true if the peer has the piece with specified index.
   *
   * @param idx The piece index
   */
  public boolean hasPiece(int idx) {
    return this.bitfield.getBit(idx);
  }

  public void setBitfield(Bitfield bitfield) {
    if (this.bitfield != Bitfield.EMPTY_BITFIELD_SINGLETON) {
      throw new IllegalArgumentException("Peer's bitfield already set.");
    }
    this.bitfield = bitfield;
  }
}
