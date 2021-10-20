package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import java.util.List;
import lombok.NonNull;

/**
 * Interface masking how we find peers.
 *
 * @author Max Yu
 */
public interface PeerLocator {
  List<Peer> locatePeers(@NonNull Torrent torrent);
}
