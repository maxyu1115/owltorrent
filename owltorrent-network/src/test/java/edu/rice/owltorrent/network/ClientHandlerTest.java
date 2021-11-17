package edu.rice.owltorrent.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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

  @Mock private PeerConnectorFactory connectorFactory;
  @Mock private SocketChannel socketChannel;
  @Mock private TorrentManager torrentManager;

  private static Torrent torrent = new Torrent();
  private static TorrentRepository torrentRepository = new TorrentRepositoryImpl();
  private static TwentyByteId peerId = TwentyByteId.fromString("OwlTorrentUser123456");
  private static TwentyByteId infoHash = TwentyByteId.fromString("12345678901234567890");

  private ClientHandler clientHandler;

  @Before
  public void init() {
    when(torrentManager.getTorrent()).thenReturn(torrent);
    clientHandler = new ClientHandler(torrentRepository, connectorFactory, socketChannel);
    torrent.setInfoHash(infoHash);
  }

  @Test
  public void testVerifyMalformedHandshake() {
    byte[] fstIncorrect = new byte[68];
    fstIncorrect[0] = 18;
    Assert.assertEquals(
        Optional.empty(), clientHandler.verifyHandShake(ByteBuffer.wrap(fstIncorrect)));

    byte[] nameIncorrect = new byte[68];
    nameIncorrect[0] = 19;
    System.arraycopy(
        "1234567890123456789".getBytes(StandardCharsets.US_ASCII), 0, nameIncorrect, 1, 19);
    Assert.assertEquals(
        Optional.empty(), clientHandler.verifyHandShake(ByteBuffer.wrap(nameIncorrect)));
  }

  @Test
  public void testVerifyMissingTorrentHandshake() {
    byte[] infoHashNotFound = new byte[68];
    infoHashNotFound[0] = 19;
    System.arraycopy(
        "BitTorrent protocol".getBytes(StandardCharsets.US_ASCII), 0, infoHashNotFound, 1, 19);
    System.arraycopy(infoHash.getBytes(), 0, infoHashNotFound, 28, 20);

    assertEquals(
        Optional.empty(), clientHandler.verifyHandShake(ByteBuffer.wrap(infoHashNotFound)));
  }

  @Test
  public void testVerifyHandshakeSuccess() {
    torrentRepository.registerTorrentManager(torrentManager);

    byte[] handshake = new byte[68];
    handshake[0] = 19;
    System.arraycopy(
        "BitTorrent protocol".getBytes(StandardCharsets.US_ASCII), 0, handshake, 1, 19);

    System.arraycopy(torrent.getInfoHash().getBytes(), 0, handshake, 28, 20);
    System.arraycopy(peerId.getBytes(), 0, handshake, 48, 20);
    TorrentManager manager = clientHandler.verifyHandShake(ByteBuffer.wrap(handshake)).get();

    assertEquals(torrent.getInfoHash(), manager.getTorrent().getInfoHash());
  }
}
