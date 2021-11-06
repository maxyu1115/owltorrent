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

  //  @Ignore // flaky test
  @Test
  public void locateWithHTTPTracker_Success() {
    String announceURL = "https://torrent.ubuntu.com/announce?";

    Torrent torrent = new Torrent();
    torrent.setInfoHash(
        new TwentyByteId(
            TorrentManager.hexStringToByteArray("32310b4db84de5c023bfcb9f40648d8c9e7ca16e")));

    try {
      HttpTrackerConnector peerLocator = new HttpTrackerConnector();
      peerLocator.locateWithHTTPTracker(
          new TorrentContext(peerId, (short) 6881, torrent), 0, 0, 0, Event.NONE, announceURL);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void createPeers_Success() throws UnknownHostException {
    byte[] addressBytes = {68, 32, 109, 105, 115, 115, 105};
    Torrent torrent = new Torrent();

    HttpTrackerConnector peerLocator = new HttpTrackerConnector();
    List<Peer> peers = peerLocator.createPeers(addressBytes, torrent);

    Assert.assertEquals(peers.get(0).getAddress().toString(), "/68.32.109.105:29555");
  }

  @Test(expected = NullPointerException.class)
  public void locateWithHTTPTracker_nullInput() throws Exception {
    HttpTrackerConnector peerLocator = new HttpTrackerConnector();
    peerLocator.locateWithHTTPTracker(null, 0, 0, 0, Event.NONE, null);
  }

  @Test(expected = NullPointerException.class)
  public void createPeers_nullInput() throws UnknownHostException {
    HttpTrackerConnector peerLocator = new HttpTrackerConnector();
    peerLocator.createPeers(null, null);
  }
}
