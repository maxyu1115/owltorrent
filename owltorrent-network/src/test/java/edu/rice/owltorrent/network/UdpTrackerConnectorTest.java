package edu.rice.owltorrent.network;

import static junit.framework.TestCase.fail;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TorrentContext;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class UdpTrackerConnectorTest {
  private static final TwentyByteId peerId = TwentyByteId.fromString("owltorrentclientpeer");

  @Test
  public void locateWithUDPTracker_Success() {
    Torrent torrent = new Torrent();
    torrent.setInfoHash(
        new TwentyByteId(
            TorrentManager.hexStringToByteArray("2b692a9c1aff75c54729ba129a3c94d2ea5d2b8c")));
    torrent.setAnnounceURL("udp://tracker.openbittorrent.com:80/announce");

    try {
      UdpTrackerConnector peerLocator = new UdpTrackerConnector();
      peerLocator.locateWithUDPTracker(new TorrentContext(peerId, (short) 6881, torrent));
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void createPeers_Success() throws UnknownHostException {
    byte[] addressBytes = {68, 32, 109, 105, 115, 115, 105};
    Torrent torrent = new Torrent();
    torrent.setInfoHash(
        new TwentyByteId(
            TorrentManager.hexStringToByteArray("2b692a9c1aff75c54729ba129a3c94d2ea5d2b8c")));
    torrent.setAnnounceURL("udp://tracker.openbittorrent.com:80/announce");

    UdpTrackerConnector peerLocator = new UdpTrackerConnector();
    List<Peer> peers = peerLocator.createPeers(addressBytes, torrent);

    Assert.assertEquals(peers.get(0).getAddress().toString(), "/68.32.109.105:29545");
  }

  @Test(expected = NullPointerException.class)
  public void locatePeers_nullInput() {
    UdpTrackerConnector peerLocator = new UdpTrackerConnector();
    peerLocator.locatePeers(null);
  }

  @Test(expected = NullPointerException.class)
  public void locateWithUDPTracker_nullInput() throws IOException {
    UdpTrackerConnector peerLocator = new UdpTrackerConnector();
    peerLocator.locateWithUDPTracker(null);
  }

  @Test(expected = NullPointerException.class)
  public void communicateWithSocket_nullInput() throws IOException {
    UdpTrackerConnector peerLocator = new UdpTrackerConnector();
    peerLocator.communicateWithSocket(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void createPeers_nullInput() throws UnknownHostException {
    UdpTrackerConnector peerLocator = new UdpTrackerConnector();
    peerLocator.createPeers(null, null);
  }
}
