package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import org.junit.Assert;
import org.junit.Test;

public class MeteringSystemTest {

  @Test
  public void addAndGetPeerMetric() {
    MeteringSystem meteringSystem = new MeteringSystem();

    TwentyByteId peerID = TwentyByteId.fromString("12345678901234567890");
    MeteringSystem.Metrics metric = MeteringSystem.Metrics.PEER_DOWNLOAD_TIME;
    double metricVal = 10;

    // add metric
    meteringSystem.addPeerMetric(peerID, metric, metricVal);

    // get metric
    Assert.assertEquals(meteringSystem.getMetric("peer", peerID, null, metric), metricVal, .001);
  }

  @Test
  public void addAndGetSystemMetric() {
    MeteringSystem meteringSystem = new MeteringSystem();

    MeteringSystem.Metrics metric = MeteringSystem.Metrics.ENTIRE_DOWNLOAD_TIME;
    double metricVal = 5.5;

    // add metric
    meteringSystem.addSystemMetric(metric, metricVal);

    // get metric
    Assert.assertEquals(meteringSystem.getMetric("system", null, null, metric), metricVal, .001);
  }

  @Test
  public void addAndGetPieceMetric() {
    MeteringSystem meteringSystem = new MeteringSystem();

    Integer pieceIndex = 12;
    MeteringSystem.Metrics metric = MeteringSystem.Metrics.PIECE_DOWNLOAD_TIME;
    double metricVal = 3.1;

    // add metric
    meteringSystem.addPieceMetric(pieceIndex, metric, metricVal);

    // get metric
    Assert.assertEquals(
        meteringSystem.getMetric("piece", null, pieceIndex, metric), metricVal, .001);
  }

  @Test
  public void rankPeersByMetric() {
    MeteringSystem meteringSystem = new MeteringSystem();

    TwentyByteId peerID1 = TwentyByteId.fromString("11111111111111111111");
    TwentyByteId peerID2 = TwentyByteId.fromString("22222222222222222222");
    TwentyByteId peerID3 = TwentyByteId.fromString("33333333333333333333");
    TwentyByteId peerID4 = TwentyByteId.fromString("44444444444444444444");
    TwentyByteId peerID5 = TwentyByteId.fromString("55555555555555555555");

    // Add peers
    meteringSystem.addPeerMetric(peerID1, MeteringSystem.Metrics.PEER_DOWNLOAD_TIME, 3.3);
    meteringSystem.addPeerMetric(peerID2, MeteringSystem.Metrics.PEER_DOWNLOAD_TIME, 5.1);
    meteringSystem.addPeerMetric(peerID3, MeteringSystem.Metrics.PEER_DOWNLOAD_TIME, 1.2);
    meteringSystem.addPeerMetric(peerID4, MeteringSystem.Metrics.PEER_DOWNLOAD_TIME, 6.8);
    meteringSystem.addPeerMetric(peerID5, MeteringSystem.Metrics.PEER_DOWNLOAD_TIME, 10.4);

    // Create local queue
    Queue<String[]> queue = new LinkedList<>();
    queue.add(new String[] {peerID3.toString(), "1.2"});
    queue.add(new String[] {peerID1.toString(), "3.3"});
    queue.add(new String[] {peerID2.toString(), "5.1"});
    queue.add(new String[] {peerID4.toString(), "6.8"});
    queue.add(new String[] {peerID5.toString(), "10.4"});

    // Fetch PQ
    PriorityQueue<String[]> rankedPeers =
        meteringSystem.rankPeersByMetric(MeteringSystem.Metrics.PEER_DOWNLOAD_TIME);

    // Ensure queue values match
    while (!rankedPeers.isEmpty()) {
      String[] peerFromRP = rankedPeers.remove();
      String[] peerFromQ = queue.remove();

      Assert.assertArrayEquals(peerFromRP, peerFromQ);
    }
  }
}
