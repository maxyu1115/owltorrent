package edu.rice.owltorrent.network;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;

import edu.rice.owltorrent.network.peerconnector.SocketConnector;
import org.junit.Before;
import org.mockito.Mock;

/**
 * Tests connection to local host.
 *
 * @author Max Yu
 */
// @RunWith(MockitoJUnitRunner.class)
public class SocketConnectorLocalHostTest {

  @Mock private TorrentRepository torrentRepository;
  @Mock private TorrentManager torrentManager;
  @Mock private Torrent torrent;

  private final TwentyByteId infoHash = TwentyByteId.fromString("12345678901234567890");

  @Mock private StorageAdapter storageAdapter;

  private HandShakeListener listener;

  @Before
  public void init() {
    when(torrentManager.getTorrent()).thenReturn(torrent);
    when(torrentRepository.retrieveTorrent(eq(infoHash))).thenReturn(Optional.of(torrentManager));
    when(torrent.getInfoHash()).thenReturn(infoHash);

    listener = new HandShakeListener(torrentRepository, 8080);
  }

  // @Test(expected = Test.None.class /* no exception expected */)
  // Commented out due to incompatible behavior with Github actions
  public void testHandShakeNoException() throws IOException {
    Thread listenerThread = new Thread(listener);
    TwentyByteId peerId = TwentyByteId.fromString("12345678901234567890");
    when(torrentManager.getOurPeerId()).thenReturn(peerId);
    try {
      listenerThread.start();

      Peer host = new Peer(peerId, new InetSocketAddress("127.0.0.1", 8080), torrent);
      SocketConnector connector =
          SocketConnector.makeInitialConnection(host, torrentManager, storageAdapter);
      connector.initiateConnection();

    } catch (Exception e) {
      listenerThread.interrupt();
      throw e;
    }

    verify(torrentRepository, times(1)).retrieveTorrent(eq(infoHash));

    listenerThread.interrupt();
  }
}
