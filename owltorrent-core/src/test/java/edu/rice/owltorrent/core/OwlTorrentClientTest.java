package edu.rice.owltorrent.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;

@Log4j2(topic = "general")
public class OwlTorrentClientTest {

  private static int findAvailablePort() throws IOException {
      try (ServerSocket ignored = new ServerSocket(0)) {
        return ignored.getLocalPort();
    } catch (IOException ignored) {
      }
    throw new IOException("No available ports found");
  }

  @Test
  public void localSeedingAndDownloadingTest() throws Exception {

    // These should be stored in the shared-resources folder.
    String pdfName = "OwlTorrentRiggedDemoPresentation.pdf";
    String torrentName = "OwlTorrentRiggedDemoPresentation.torrent";

    final int seederPort = findAvailablePort();

    URI pdfUrl = OwlTorrentClientTest.class.getClassLoader().getResource(pdfName).toURI();
    String pdfPath = pdfUrl.getPath();
    URI torrentUrl = OwlTorrentClientTest.class.getClassLoader().getResource(torrentName).toURI();
    String torrentPath = torrentUrl.getPath();

    // Need to delete local version of pdf in case this test was already run, avoids a
    // FileAlreadyExists exception.
    File existing = new File(pdfName);
    existing.delete();

    TwentyByteId seederId = OwlTorrentClient.generateRandomPeerId();
    PeerLocator mockedSeederLocator = mock(PeerLocator.class);
    when(mockedSeederLocator.locatePeers(any(), anyLong(), anyLong(), anyLong(), any()))
        .thenReturn(List.of());
    OwlTorrentClient seeder = new OwlTorrentClient(seederPort, mockedSeederLocator, seederId);
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
    // waiting but this sleep is fine for now.
    Thread.sleep(1000);

    final int downloaderPort = findAvailablePort();
    TwentyByteId downloaderId = OwlTorrentClient.generateRandomPeerId();
    PeerLocator mockedDownloaderLocator = mock(PeerLocator.class);
    Torrent torrent = TorrentParser.parse(new File(torrentPath));
    when(mockedDownloaderLocator.locatePeers(any(), anyLong(), anyLong(), anyLong(), any()))
        .thenReturn(
            List.of(new Peer(seederId, new InetSocketAddress("localhost", seederPort), torrent)));
    OwlTorrentClient downloader =
        new OwlTorrentClient(downloaderPort, mockedDownloaderLocator, downloaderId);
    OwlTorrentClient.ProgressMeter meter = downloader.downloadFile(torrentPath);

    // Again this is a bit of a hack, if we don't download the file within a second we throw an
    // error
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < 1000 && meter.ratioCompleted() < 1.0) {
      Thread.sleep(10);
    }
    assertEquals(1.0, meter.ratioCompleted(), 0);

    // Check files are equal
    byte[] f1 = Files.readAllBytes(Path.of(pdfName));
    byte[] f2 = Files.readAllBytes(Paths.get(pdfUrl));
    assertArrayEquals(f1, f2);
  }

  @Test
  public void testFindListenerPort() throws IOException {
    int firstPort = OwlTorrentClient.findAvailablePort();
    assertEquals(
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
