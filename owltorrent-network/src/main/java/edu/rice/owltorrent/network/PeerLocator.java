package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TorrentContext;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;

/**
 * Interface masking how we find peers.
 *
 * @author Max Yu
 */
public interface PeerLocator {
  enum Event {
    NONE(0),
    COMPLETED(1),
    STARTED(2),
    STOPPED(3);

    Event(int code) {
      this.eventCode = code;
    }

    @Getter private final int eventCode;
  }

  List<Peer> locatePeers(
      @NonNull TorrentContext torrentContext,
      long downloaded,
      long left,
      long uploaded,
      Event event);
}
