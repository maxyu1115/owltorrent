package edu.rice.owltorrent.network;

import static junit.framework.TestCase.fail;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TorrentContext;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class HttpTrackerConnectorTest {
  private static final TwentyByteId peerId = TwentyByteId.fromString("owltorrentclientpeer");

  @Test
  public void locateWithHTTPTracker() {
    Torrent torrent = new Torrent();
    torrent.setAnnounceURL("https://torrent.ubuntu.com/announce?");
    torrent.setInfoHash(
        new TwentyByteId(
            TorrentManager.hexStringToByteArray("32310b4db84de5c023bfcb9f40648d8c9e7ca16e")));

    try {
      HttpTrackerConnector peerLocator = new HttpTrackerConnector();
      peerLocator.locateWithHTTPTracker(
          new TorrentContext(peerId, (short) 6881, torrent), 0, 0, 0, PeerLocator.Event.NONE);
    } catch (Exception e) {
      e.printStackTrace();
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
    HttpTrackerConnector peerLocator = new HttpTrackerConnector();
    peerLocator.locatePeers(null, 0, 0, 0, PeerLocator.Event.NONE);
  }

  @Test(expected = NullPointerException.class)
  public void locateWithHTTPTracker_nullInput() throws Exception {
    HttpTrackerConnector peerLocator = new HttpTrackerConnector();
    peerLocator.locateWithHTTPTracker(null, 0, 0, 0, PeerLocator.Event.NONE);
  }

  @Test(expected = NullPointerException.class)
  public void createPeers_nullInput() throws UnknownHostException {
    HttpTrackerConnector peerLocator = new HttpTrackerConnector();
    peerLocator.createPeers(null, null);
  }
}
