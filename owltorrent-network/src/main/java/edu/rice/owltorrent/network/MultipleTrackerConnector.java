package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TorrentContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Multiple tracker connector to retrieve all peers at once (supports HTTP and UDP).
 *
 * @author bhaveshshah
 */
@Log4j2(topic = "network")
public class MultipleTrackerConnector implements PeerLocator {
  public static final HttpTrackerConnector httpTrackerConnector = new HttpTrackerConnector();
  public static final UdpTrackerConnector udpTrackerConnector = new UdpTrackerConnector();

  /**
   * Retrieve all peers from all available trackers.
   *
   * @param torrentContext Torrent Context object
   * @return list of peers
   */
  @Override
  public List<Peer> locatePeers(
      @NonNull TorrentContext torrentContext,
      long downloaded,
      long left,
      long uploaded,
      Event event) {
    List<String> announceUrls = torrentContext.getTorrent().getAnnounceUrls();
    Set<Peer> peerSet = new HashSet<>(); // Use set to get rid of duplicate peers.

    // Iterate all announceUrls present in tracker.
    for (String announceUrl : announceUrls) {
      String protocol = announceUrl.split(":")[0];

      try {
        if (protocol.equals("http") || protocol.equals("https")) {
          peerSet.addAll(
              httpTrackerConnector.locateWithHTTPTracker(
                  torrentContext, downloaded, left, uploaded, event, announceUrl));
        } else if (protocol.equals("udp")) {
          peerSet.addAll(
              udpTrackerConnector.locateWithUDPTracker(
                  torrentContext, downloaded, left, uploaded, event, announceUrl));
        } else {
          log.error("Tracker protocol not supported: " + protocol);
        }
      } catch (Exception e) {
        log.error(e);
        continue;
      }
    }

    return new ArrayList<Peer>(peerSet);
  }
}
