package edu.rice.owltorrent.core;

import edu.rice.owltorrent.common.entity.Torrent;
import java.util.List;

public interface PeerLocator {
  List<Peer> locatePeers(Torrent torrent);
}
