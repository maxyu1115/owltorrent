package edu.rice.owltorrent.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests client handler.
 *
 * @author Max Yu
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientHandlerTest {

  @Mock private TorrentRepository torrentRepository;
  @Mock private Socket socket;
  @Mock private TorrentManager torrentManager;

  private Torrent torrent = new Torrent();

  private ClientHandler clientHandler;

  @Before
  public void init() {
    when(torrentManager.getTorrent()).thenReturn(torrent);
    clientHandler = new ClientHandler(torrentRepository, socket);
  }

  @Test
  public void testVerifyMalformedHandshake() {
    byte[] fstIncorrect = new byte[68];
    fstIncorrect[0] = 18;
    Assert.assertEquals(Optional.empty(), clientHandler.verifyHandShake(fstIncorrect));

    byte[] nameIncorrect = new byte[68];
    nameIncorrect[0] = 19;
    System.arraycopy(
        "1234567890123456789".getBytes(StandardCharsets.US_ASCII), 0, nameIncorrect, 1, 19);
    Assert.assertEquals(Optional.empty(), clientHandler.verifyHandShake(nameIncorrect));
  }

  @Test
  public void testVerifyMissingTorrentHandshake() {
    TwentyByteId infoHash = TwentyByteId.fromString("12345678901234567890");

    byte[] infoHashNotFound = new byte[68];
    infoHashNotFound[0] = 19;
    System.arraycopy(
        "BitTorrent protocol".getBytes(StandardCharsets.US_ASCII), 0, infoHashNotFound, 1, 19);
    System.arraycopy(infoHash.getBytes(), 0, infoHashNotFound, 28, 20);

    when(torrentRepository.retrieveTorrent(eq(infoHash))).thenReturn(Optional.empty());

    assertEquals(Optional.empty(), clientHandler.verifyHandShake(infoHashNotFound));
    verify(torrentRepository, times(1)).retrieveTorrent(eq(infoHash));
  }

  @Test
  public void testVerifyHandshakeSuccess() {
    TwentyByteId infoHash = TwentyByteId.fromString("12345678901234567890");
    TwentyByteId peerId = TwentyByteId.fromString("OwlTorrentUser123456");

    torrent.setInfoHash(infoHash);

    byte[] handshake = new byte[68];
    handshake[0] = 19;
    System.arraycopy(
        "BitTorrent protocol".getBytes(StandardCharsets.US_ASCII), 0, handshake, 1, 19);

    System.arraycopy(infoHash.getBytes(), 0, handshake, 28, 20);

    System.arraycopy(peerId.getBytes(), 0, handshake, 48, 20);

    when(torrentRepository.retrieveTorrent(eq(infoHash))).thenReturn(Optional.of(torrentManager));

    Peer foundPeer = clientHandler.verifyHandShake(handshake).get();

    assertEquals(peerId, foundPeer.getPeerID());
    assertEquals(torrent, foundPeer.getTorrent());

    verify(torrentRepository, times(1)).retrieveTorrent(eq(infoHash));
  }
}
