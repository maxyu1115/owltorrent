package edu.rice.owltorrent.network;

import com.google.common.util.concurrent.AtomicDouble;
import edu.rice.owltorrent.common.entity.TwentyByteId;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metering system to keep track of peer, system, and piece-related metrics.
 *
 * @author bhaveshshah
 */
public class MeteringSystem {
  public enum Metrics {
    // Peer-related metrics
    PEER_DOWNLOAD_TIME,

    // System-related metrics
    ENTIRE_DOWNLOAD_TIME,
    EFFECTIVE_PIECES,
    FAILED_PIECES,

    // Piece-related metrics
    PIECE_DOWNLOAD_TIME
  }

  // Maps to store metrics and associated values
  public static ConcurrentHashMap<TwentyByteId, ConcurrentHashMap<Enum<?>, AtomicDouble>>
      peerMetrics;
  public static ConcurrentHashMap<Enum<?>, AtomicDouble> systemMetrics;
  public static ConcurrentHashMap<Integer, ConcurrentHashMap<Enum<?>, AtomicDouble>> pieceMetrics;

  public MeteringSystem() {
    this.peerMetrics = new ConcurrentHashMap<>();
    this.systemMetrics = new ConcurrentHashMap<>();
    this.pieceMetrics = new ConcurrentHashMap<>();
  }

  /**
   * Get metric for a specified peer.
   *
   * @param metricType type of metric (system, peer, piece)
   * @param peerID peer id
   * @param pieceIndex index of piece
   * @param metricName metric to track
   * @return metric value for peer
   */
  public AtomicDouble getMetric(
      String metricType, TwentyByteId peerID, Integer pieceIndex, Enum<?> metricName) {
    AtomicDouble defaultVal = new AtomicDouble(0);

    if (metricType.equals("peer")) {
      if (peerID == null || !peerMetrics.containsKey(peerID)) {
        return defaultVal;
      }
      return peerMetrics.get(peerID).getOrDefault(metricName, defaultVal);
    } else if (metricType.equals("system")) {
      return systemMetrics.getOrDefault(metricName, defaultVal);
    } else if (metricType.equals("piece")) {
      if (pieceIndex == null || !pieceMetrics.containsKey(pieceIndex)) {
        return defaultVal;
      }
      return pieceMetrics.get(pieceIndex).getOrDefault(metricName, defaultVal);
    } else {
      return defaultVal;
    }
  }

  /**
   * Add or update system-wide metric.
   *
   * @param metricName metric to track
   * @param metricVal numerical value of metric
   */
  public void addSystemMetric(Enum<?> metricName, double metricVal) {
    // Update or create new entry in system map
    double currVal = systemMetrics.getOrDefault(metricName, new AtomicDouble(0)).doubleValue();
    systemMetrics.put(metricName, new AtomicDouble(currVal + metricVal));

    return;
  }

  /**
   * Add or update metric for a given peer.
   *
   * @param peerID peer id
   * @param metricName metric to track
   * @param metricVal numerical value of metric
   */
  public void addPeerMetric(TwentyByteId peerID, Enum<?> metricName, double metricVal) {
    ConcurrentHashMap<Enum<?>, AtomicDouble> metricsForPeer;

    if (peerMetrics.containsKey(peerID)) {
      metricsForPeer = peerMetrics.get(peerID);

      // Update or create new metric entry for peer
      double currVal = metricsForPeer.getOrDefault(metricName, new AtomicDouble(0)).doubleValue();
      metricsForPeer.put(metricName, new AtomicDouble(currVal + metricVal));
    } else {
      // Create new peer and corresponding metric entry
      metricsForPeer = new ConcurrentHashMap<>();
      metricsForPeer.put(metricName, new AtomicDouble(metricVal));
    }

    // Update outer hashmap
    peerMetrics.put(peerID, metricsForPeer);

    return;
  }

  /**
   * Add or update metric for a given piece.
   *
   * @param pieceIndex index of piece
   * @param metricName metric to track
   * @param metricVal numerical value of metric
   */
  public void addPieceMetric(Integer pieceIndex, Enum<?> metricName, double metricVal) {
    ConcurrentHashMap<Enum<?>, AtomicDouble> metricsForPiece;

    if (pieceMetrics.containsKey(pieceIndex)) {
      metricsForPiece = pieceMetrics.get(pieceIndex);

      // Update or create new metric entry for piece
      double currVal = metricsForPiece.getOrDefault(metricName, new AtomicDouble(0)).doubleValue();
      metricsForPiece.put(metricName, new AtomicDouble(currVal + metricVal));
    } else {
      // Create new piece and corresponding metric entry
      metricsForPiece = new ConcurrentHashMap<>();
      metricsForPiece.put(metricName, new AtomicDouble(metricVal));
    }

    // Update outer hashmap
    pieceMetrics.put(pieceIndex, metricsForPiece);

    return;
  }

  /**
   * Rank peers by specified metric.
   *
   * @param metricName metric to rank by
   * @return ranked peers
   */
  public PriorityQueue<String[]> rankPeersByMetric(Enum<?> metricName) {
    // Create priority queue for peers
    PriorityQueue<String[]> rankedPeers =
        new PriorityQueue<>(
            new Comparator<String[]>() {
              @Override
              public int compare(String[] peer1, String[] peer2) {
                float peer1MetricVal = Float.parseFloat(peer1[1]);
                float peer2MetricVal = Float.parseFloat(peer2[1]);

                return (peer1MetricVal > peer2MetricVal) ? 1 : -1;
              }
            });

    // Populate queue
    for (Map.Entry<TwentyByteId, ConcurrentHashMap<Enum<?>, AtomicDouble>> peer :
        peerMetrics.entrySet()) {
      // Only add peers that contain specified metric
      if (peer.getValue().containsKey(metricName)) {
        AtomicDouble metricVal = peer.getValue().get(metricName);
        String[] peerMetricArr = {peer.getKey().toString(), String.valueOf(metricVal)};

        rankedPeers.add(peerMetricArr);
      }
    }

    return rankedPeers;
  }
}

// ------ peerMetrics schema ------
// peer1:
        // PEER_DOWNLOAD_TIME: 10.5

// peer2
        // PEER_DOWNLOAD_TIME: 21

// ------ systemMetrics schema ------
// ENTIRE_DOWNLOAD_TIME: 30.5
// EFFECTIVE_PIECES: 20
// FAILED_PIECES: 1

// ------ pieceMetrics schema ------
// index1:
    // PIECE_DOWNLOAD_TIME: 1.7

// index2
    // PIECE_DOWNLOAD_TIME: 1.8
