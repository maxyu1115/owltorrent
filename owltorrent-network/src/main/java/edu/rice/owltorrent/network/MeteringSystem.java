package edu.rice.owltorrent.network;

import com.google.common.util.concurrent.AtomicDouble;
import edu.rice.owltorrent.common.entity.TwentyByteId;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metering system to keep track of various peer-related metrics.
 */
public class MeteringSystem {
    public enum Metrics {
        DOWNLOAD_TIME,
        EFFECTIVE_PIECES,
        FAILED_PIECES
    }

    public static ConcurrentHashMap<TwentyByteId, ConcurrentHashMap<Enum<?>, AtomicDouble>> metricMap;

    public MeteringSystem() {
        this.metricMap = new ConcurrentHashMap<>();
    }

    /**
     * Add or update metric for a given peer.
     *
     * @param peerID peer id
     * @param metricName metric to track
     * @param metricVal numerical value of metric
     */
    public void addMetric(TwentyByteId peerID, Enum<?> metricName, double metricVal) {
        ConcurrentHashMap<Enum<?>, AtomicDouble> metricsForPeer;

        if (metricMap.containsKey(peerID)) {
            metricsForPeer = metricMap.get(peerID);

            // Update or create new entry in peer map
            double currVal = metricsForPeer.getOrDefault(metricName,
                    new AtomicDouble(0)).doubleValue();
            metricsForPeer.put(metricName, new AtomicDouble(currVal + metricVal));
        } else {
            metricsForPeer = new ConcurrentHashMap<>();
            metricsForPeer.put(metricName, new AtomicDouble(metricVal));
        }

        // Update outer hashmap
        metricMap.put(peerID, metricsForPeer);

        return;
    }

    /**
     * Get metric for a specified peer.
     *
     * @param peerID peer id
     * @param metricName metric to track
     * @return metric value for peer
     */
    public AtomicDouble getMetric(TwentyByteId peerID, Enum<?> metricName) {
        return metricMap.get(peerID).get(metricName);
    }

    /**
     * Rank peers by specified metric.
     *
     * @param metricName metric to rank by
     * @return ranked peers
     */
    public PriorityQueue<String[]> rankPeersByMetric(Enum<?> metricName) {
        // Create priority queue for peers
        PriorityQueue<String[]> rankedPeers = new PriorityQueue<>(new Comparator<String[]>() {
            @Override
            public int compare(String[] peer1, String[] peer2) {
                float peer1MetricVal = Float.parseFloat(peer1[1]);
                float peer2MetricVal = Float.parseFloat(peer2[1]);

                return (peer1MetricVal > peer2MetricVal) ? 1 : -1;
            }
        });

        // Populate queue
        for (Map.Entry<TwentyByteId, ConcurrentHashMap<Enum<?>, AtomicDouble>> peer : metricMap.entrySet()) {
            if (peer.getValue().containsKey(metricName)) {
                AtomicDouble metricVal = peer.getValue().get(metricName);
                String[] peerMetricArr = {String.valueOf(metricName), String.valueOf(metricVal)};

                rankedPeers.add(peerMetricArr);
            }
        }

        return rankedPeers;
    }
}

// schema:
// peer1:
        // peersFailed: 20
        // latency: 10.5

// peer2
        // peersFailed: 30
        // latency: 21