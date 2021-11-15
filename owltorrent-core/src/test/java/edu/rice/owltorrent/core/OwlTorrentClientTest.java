package edu.rice.owltorrent.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.core.serialization.TorrentParser;
import edu.rice.owltorrent.network.PeerLocator;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;

@Log4j2(topic = "general")
public class OwlTorrentClientTest {
  @Test
  public void localSeedingAndDownloadingTest() throws Exception {

    File existing = new File("OwlTorrentRiggedDemoPresentation.pdf");
    existing.delete();

    URI pdfUrl =
        OwlTorrentClientTest.class
            .getClassLoader()
            .getResource("OwlTorrentRiggedDemoPresentation.pdf")
            .toURI();
    String pdfPath = pdfUrl.getPath();
    URI torrentUrl =
        OwlTorrentClientTest.class
            .getClassLoader()
            .getResource("OwlTorrentRiggedDemoPresentation.torrent")
            .toURI();
    String torrentPath = torrentUrl.getPath();

    TwentyByteId seederId = OwlTorrentClient.generateRandomPeerId();
    PeerLocator mockedSeederLocator = mock(PeerLocator.class);
    when(mockedSeederLocator.locatePeers(any(), anyLong(), anyLong(), anyLong(), any()))
        .thenReturn(List.of());
    OwlTorrentClient seeder = new OwlTorrentClient(57601, mockedSeederLocator, seederId);
    seeder.startSeeding();
    new Thread(
            () -> {
              try {
                seeder.seedFile(torrentPath, pdfPath);
              } catch (Exception e) {
                e.printStackTrace();
              }
            })
        .start();

    // Make sure that this seeder actually starts seeding, technically should do some sort of
    // waiting but this sleep
    // is fine for now.
    Thread.sleep(100);

    TwentyByteId downloaderId = OwlTorrentClient.generateRandomPeerId();
    PeerLocator mockedDownloaderLocator = mock(PeerLocator.class);
    Torrent torrent = TorrentParser.parse(new File(torrentPath));
    when(mockedDownloaderLocator.locatePeers(any(), anyLong(), anyLong(), anyLong(), any()))
        .thenReturn(
            List.of(new Peer(seederId, new InetSocketAddress("localhost", 57601), torrent)));
    OwlTorrentClient downloader =
        new OwlTorrentClient(57600, mockedDownloaderLocator, downloaderId);
    OwlTorrentClient.ProgressMeter meter = downloader.downloadFile(torrentPath);

    while (meter.getPercentDone() < 1.0) {
      Thread.sleep(100);
    }
  }

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
