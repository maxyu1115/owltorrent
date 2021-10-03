package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Torrent;
import org.junit.Test;

public class PeerLocatorTest {

    @Test
    public void locatePeers() {
        Torrent torrent = new Torrent();
        PeerLocator peerLocator = new PeerLocator();
        peerLocator.locatePeers(torrent);
    }

    @Test(expected = NullPointerException.class)
    public void locatePeers_nullInput() {
        PeerLocator peerLocator = new PeerLocator();
        peerLocator.locatePeers(null);
    }

}