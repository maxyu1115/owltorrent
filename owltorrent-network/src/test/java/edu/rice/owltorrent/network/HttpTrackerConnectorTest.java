package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Torrent;
import org.junit.Test;

public class HttpTrackerConnectorTest {

  @Test
  public void locatePeers() {
    Torrent torrent = new Torrent();
    HttpTrackerConnector httpTrackerConnector = new HttpTrackerConnector();
    httpTrackerConnector.locatePeers(torrent);
  }

  @Test(expected = NullPointerException.class)
  public void locatePeers_nullInput() {
    HttpTrackerConnector httpTrackerConnector = new HttpTrackerConnector();
    httpTrackerConnector.locatePeers(null);
  }
}
