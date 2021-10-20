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
  @EqualsAndHashCode.Include @Getter private TwentyByteId peerID;
  /** NOTE: address may be null!!! (for peers that connected to us) */
  @Getter private final InetSocketAddress address;

  @EqualsAndHashCode.Include @Getter private final Torrent torrent;

  private final AtomicBoolean interested = new AtomicBoolean(false);
  private final AtomicBoolean choked = new AtomicBoolean(true);

  public Peer(TwentyByteId peerID, InetSocketAddress address, Torrent torrent) {
    this.peerID = peerID;
    this.address = address;
    this.torrent = torrent;
  }

  public void setPeerID(TwentyByteId peerID) {
    if (this.peerID != null) {
      throw new IllegalStateException("This Peer's ID is already set");
    }
    this.peerID = peerID;
  }

  public void setInterested(boolean interested) {
    this.interested.set(interested);
  }

  public boolean isInterested() {
    return this.interested.get();
  }

  public void setChoked(boolean choked) {
    this.interested.set(choked);
  }

  public boolean isChoked() {
    return this.choked.get();
  }
}
