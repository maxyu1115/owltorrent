package edu.rice.owltorrent.network;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.rice.owltorrent.common.adapters.NetworkToStorageAdapter;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.common.interfaces.TorrentRepository;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests connection to local host.
 *
 * @author Max Yu
 */
@RunWith(MockitoJUnitRunner.class)
public class SocketConnectorLocalHostTest {

  @Mock private TorrentRepository torrentRepository;
  @Mock private Torrent torrent;

  private final TwentyByteId infoHash = TwentyByteId.fromString("12345678901234567890");

  @Mock private NetworkToStorageAdapter networkToStorageAdapter;

  private HandShakeListener listener;

  @Before
  public void init() {
    when(torrentRepository.retrieveTorrent(eq(infoHash))).thenReturn(Optional.of(torrent));
    when(torrent.getInfoHash()).thenReturn(infoHash);

    listener = new HandShakeListener(torrentRepository, 8080);
  }

  @Test(expected = Test.None.class /* no exception expected */)
  public void testHandShakeNoException() throws IOException {
    Thread listenerThread = new Thread(listener);
    TwentyByteId peerId = TwentyByteId.fromString("12345678901234567890");
    try {
      listenerThread.start();

      Peer host = new Peer(peerId, new InetSocketAddress("127.0.0.1", 8080), torrent);
      SocketConnector connector = SocketConnector.initiateConnection(host, networkToStorageAdapter);

    } catch (Exception e) {
      listenerThread.interrupt();
      throw e;
    }

    verify(torrentRepository, times(1)).retrieveTorrent(eq(infoHash));

    listenerThread.interrupt();
  }
}