package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TorrentContext;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class MultipleTrackerConnectorTest {
  private static final TwentyByteId peerId = TwentyByteId.fromString("owltorrentclientpeer");

  @Test
  public void locatePeers_Success() {
    List<String> announceUrls =
        new ArrayList<>(
            Arrays.asList(
                new String[] {
                  "udp://tracker.openbittorrent.com:80/announce",
                  "udp://tracker.opentrackr.org:1337/announce"
                }));
    Torrent torrent = new Torrent();
    torrent.setAnnounceUrls(announceUrls);
    torrent.setInfoHash(
        new TwentyByteId(
            TorrentManager.hexStringToByteArray("2b692a9c1aff75c54729ba129a3c94d2ea5d2b8c")));

    PeerLocator locator = new MultipleTrackerConnector();
    List<Peer> peers =
        locator.locatePeers(new TorrentContext(peerId, (short) 6881, torrent), 0, 0, 0, Event.NONE);

    Assert.assertTrue(peers.size() > 0); // Not always true
  }

  @Test(expected = NullPointerException.class)
  public void locatePeers_nullInput() throws Exception {
    PeerLocator locator = new MultipleTrackerConnector();
    locator.locatePeers(null, 0, 0, 0, Event.NONE);
    ;
  }
}
