package edu.rice.owltorrent.common.entity;

import edu.rice.owltorrent.common.util.AtomicHashableBoolean;
import java.net.InetSocketAddress;
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
public class Peer {
  @Getter private final TwentyByteId peerID;
  /** NOTE: address may be null!!! (for peers that connected to us) */
  @Getter private final InetSocketAddress address;

  @Getter private final Torrent torrent;

  private final AtomicHashableBoolean interested = new AtomicHashableBoolean(false);
  private final AtomicHashableBoolean choked = new AtomicHashableBoolean(true);
  private final

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
