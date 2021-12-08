package edu.rice.owltorrent.network;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.*;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.network.messages.PieceActionMessage;
import edu.rice.owltorrent.network.messages.PieceMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestMessageHandlerTest {
  private static final TwentyByteId peerId = TwentyByteId.fromString("owltorrentclientpeer");

  private TorrentManager manager;

  private MessageHandler messageHandler;

  @Mock PeerConnectorFactory factory;

  @Mock PeerConnector conn;

  @Mock StorageAdapter storageAdapter;

  @Mock Torrent torrent;

  @Mock Peer peer;

  @Before
  public void init() {
    manager =
        new TorrentManager(
            new TorrentContext(peerId, (short) 8080, torrent),
            storageAdapter,
            factory,
            new MultipleTrackerConnector());
    messageHandler = manager.getMessageHandler();
  }

  @Test
  public void testHandleNotHave() throws IOException, InterruptedException {
    PieceActionMessage correctMsg = PieceActionMessage.makeRequestMessage(1, 20, 10);
    List<byte[]> testList = new ArrayList<>();
    testList.add(new byte[] {});
    testList.add(new byte[] {});
    when(conn.getPeer()).thenReturn(peer);

    when(torrent.getPieceHashes()).thenReturn(testList);
    when(torrent.getPieceLength()).thenReturn((long) 128);
    when(peer.isPeerInterested()).thenReturn(true);
    when(peer.isAmChoked()).thenReturn(false);

    messageHandler.handleMessage(correctMsg, conn);

    // The torrent manage shouldn't send a response since it doesn't have the piece
    verify(conn, times(0)).sendMessage(eq(new PieceMessage(1, 20, new byte[] {(byte) 0x20})));
  }

  @Test
  public void testHandleIncorrect() throws Exceptions.IllegalByteOffsets, IOException {
    PieceActionMessage incorrectMsg = spy(PieceActionMessage.makeRequestMessage(1, 20, 10));
    List<byte[]> testList = new ArrayList<>();
    testList.add(new byte[] {});
    testList.add(new byte[] {});
    when(conn.getPeer()).thenReturn(peer);

    when(torrent.getPieceHashes()).thenReturn(testList);
    when(torrent.getPieceLength()).thenReturn((long) 25);

    when(peer.isPeerInterested()).thenReturn(true);
    when(peer.isAmChoked()).thenReturn(false);

    boolean noError = true;
    try {
      messageHandler.handleMessage(incorrectMsg, conn);
    } catch (InterruptedException e) {
      noError = false;
    }
    Assert.assertFalse(noError);

    verify(incorrectMsg, times(1)).verify(eq(torrent));
    verify(storageAdapter, times(0)).read(any());
  }

  @Test
  public void testHandleChoke()
      throws Exceptions.IllegalByteOffsets, IOException, InterruptedException {
    PieceActionMessage correctMsg = spy(PieceActionMessage.makeRequestMessage(1, 20, 10));
    List<byte[]> testList = new ArrayList<>();
    testList.add(new byte[] {});
    testList.add(new byte[] {});
    when(conn.getPeer()).thenReturn(peer);

    when(peer.isPeerInterested()).thenReturn(true);
    when(peer.isAmChoked()).thenReturn(true);

    messageHandler.handleMessage(correctMsg, conn);

    verify(correctMsg, times(0)).verify(eq(torrent));
    verify(storageAdapter, times(0)).read(any());
  }

  @Test
  public void testHandleInterested()
      throws Exceptions.IllegalByteOffsets, IOException, InterruptedException {
    PieceActionMessage correctMsg = spy(PieceActionMessage.makeRequestMessage(1, 20, 10));
    List<byte[]> testList = new ArrayList<>();
    testList.add(new byte[] {});
    testList.add(new byte[] {});
    when(conn.getPeer()).thenReturn(peer);

    when(peer.isPeerInterested()).thenReturn(false);

    messageHandler.handleMessage(correctMsg, conn);

    verify(correctMsg, times(0)).verify(eq(torrent));
    verify(storageAdapter, times(0)).read(any());
  }
}
