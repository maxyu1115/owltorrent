package edu.rice.owltorrent.core;

import java.io.IOException;
import java.net.ServerSocket;
import org.junit.Assert;
import org.junit.Test;

public class OwlTorrentClientTest {

  @Test
  public void testFindListenerPort() throws IOException {
    int firstPort = OwlTorrentClient.findAvailablePort();
    Assert.assertEquals(
        "Test the function doesn't use any additional ports",
        firstPort,
        OwlTorrentClient.findAvailablePort());

    ServerSocket socket = new ServerSocket(firstPort);
    Assert.assertTrue(
        "Test after occupying the first port, the method gives us a new port",
        firstPort < OwlTorrentClient.findAvailablePort());
    socket.close();
  }
}
