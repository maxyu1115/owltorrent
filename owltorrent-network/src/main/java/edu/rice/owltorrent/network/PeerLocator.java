package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import java.util.List;

public interface PeerLocator {
  List<Peer> locatePeers(Torrent torrent);
}
