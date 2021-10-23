package edu.rice.owltorrent.network;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.network.messages.PieceActionMessage;
import edu.rice.owltorrent.network.messages.PieceMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestMessageHandlerTest {
  @Mock SocketConnector conn;

  @Mock TorrentManager manager;

  @Mock StorageAdapter storageAdapter;

  @Mock Torrent torrent;

  @Test
  public void testHandleCorrect() throws Exceptions.IllegalByteOffsets, IOException {
    PieceActionMessage correctMsg = PieceActionMessage.makeRequestMessage(1, 20, 10);
    List<byte[]> testList = new ArrayList<>();
    testList.add(new byte[] {});
    testList.add(new byte[] {});
    conn.manager = manager;
    conn.storageAdapter = storageAdapter;

    when(torrent.getPieces()).thenReturn(testList);
    when(torrent.getPieceLength()).thenReturn((long) 128);
    when(conn.manager.getTorrent()).thenReturn(torrent);
    when(conn.storageAdapter.read(any()))
        .thenReturn(new FileBlock(1, 20, new byte[] {(byte) 0x20}));

    conn.handleMessage(correctMsg);

    verify(conn, times(1)).writeMessage(eq(new PieceMessage(1, 20, new byte[] {(byte) 0x20})));
  }

  @Test
  public void testHandleIncorrect() throws Exceptions.IllegalByteOffsets, IOException {
    PieceActionMessage incorrectMsg = spy(PieceActionMessage.makeRequestMessage(1, 20, 10));
    List<byte[]> testList = new ArrayList<>();
    testList.add(new byte[] {});
    testList.add(new byte[] {});
    conn.manager = manager;
    conn.storageAdapter = storageAdapter;

    when(torrent.getPieces()).thenReturn(testList);
    when(torrent.getPieceLength()).thenReturn((long) 25);
    when(conn.manager.getTorrent()).thenReturn(torrent);

    conn.handleMessage(incorrectMsg);

    verify(incorrectMsg, times(1)).verify(eq(torrent));
    verify(storageAdapter, times(0)).read(any());
  }
}
