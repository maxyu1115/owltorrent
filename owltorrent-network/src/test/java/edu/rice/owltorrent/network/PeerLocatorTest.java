package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;

import static junit.framework.TestCase.fail;

public class PeerLocatorTest {

  @Test
  public void locateWithHTTPTracker() {
    Torrent torrent = new Torrent();
    torrent.setAnnounceURL("https://torrent.ubuntu.com/announce?");

    try {
      PeerLocator peerLocator = new PeerLocator();
      peerLocator.locateWithHTTPTracker(torrent);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void locateWithUDPTracker() {
    Torrent torrent = new Torrent();
    torrent.setInfoHash(new TwentyByteId(TorrentManager.hexStringToByteArray("2b692a9c1aff75c54729ba129a3c94d2ea5d2b8c")));
    torrent.setAnnounceURL("udp://tracker.openbittorrent.com:80/announce");

    try {
      PeerLocator peerLocator = new PeerLocator();
      peerLocator.locateWithUDPTracker(torrent);
    } catch (Exception e) {
      fail();
    }
  }

  @Test(expected = NullPointerException.class)
  public void locatePeers_nullInput() {
    PeerLocator peerLocator = new PeerLocator();
    peerLocator.locatePeers(null);
  }

  @Test(expected = NullPointerException.class)
  public void locateWithHTTPTracker_nullInput() throws IOException {
    PeerLocator peerLocator = new PeerLocator();
    peerLocator.locateWithHTTPTracker(null);
  }

  @Test(expected = NullPointerException.class)
  public void locateWithUDPTracker_nullInput() throws IOException {
    PeerLocator peerLocator = new PeerLocator();
    peerLocator.locateWithUDPTracker(null);
  }

  @Test(expected = NullPointerException.class)
  public void communicateWithSocket_nullInput() throws IOException {
    PeerLocator peerLocator = new PeerLocator();
    peerLocator.communicateWithSocket(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void parsePeers_nullInput() throws UnknownHostException {
    PeerLocator peerLocator = new PeerLocator();
    peerLocator.parsePeers(null);
  }
}
