package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Torrent;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeerLocatorTest {

    @Test
    public void locatePeers() {
        Torrent torrent = new Torrent();
        PeerLocator peerLocator = new PeerLocator();
        peerLocator.locatePeers(torrent);
    }
}