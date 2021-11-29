package edu.rice.owltorrent.network;

import com.google.common.util.concurrent.AtomicDouble;
import edu.rice.owltorrent.common.entity.TwentyByteId;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MeteringSystem {
    public enum Metrics {
        EFFECTIVE_PIECES,
        FAILED_PIECES
    }

    public static ConcurrentHashMap<TwentyByteId, ConcurrentHashMap<Enum<?>, AtomicDouble>> metricMap;

    public MeteringSystem() {
        this.metricMap = new ConcurrentHashMap<>();
    }

    public void addMetric(TwentyByteId peerID, Enum<?> metricName, AtomicDouble metricVal) {
        ConcurrentHashMap<Enum<?>, AtomicDouble> metricsForPeer;

        if (metricMap.containsKey(peerID)) {
            metricsForPeer = metricMap.get(peerID);

            // Update or create new entry in peer map
            metricsForPeer.put(metricName, metricsForPeer.getOrDefault(metricName,
                    new AtomicDouble(0)).getAndAdd(metricVal));
        } else {
            metricsForPeer = new ConcurrentHashMap<>();
            metricsForPeer.put(metricName, metricVal);
        }

        // Update outer hashmap
        metricMap.put(peerID, metricsForPeer);

        return;
    }

    public AtomicDouble getMetric(TwentyByteId peerID, Enum<?> metricName) {
        return metricMap.get(peerID).get(metricName);
    }

    public PriorityQueue<String[]> rankPeersByMetric(Enum<?> metricName) {
        PriorityQueue<String[]> rankedPeers = new PriorityQueue<>(new Comparator<String[]>() {
            @Override
            public int compare(String[] peer1, String[] peer2) {
                float peer1MetricVal = Float.parseFloat(peer1[1]);
                float peer2MetricVal = Float.parseFloat(peer2[1]);

                return (peer1MetricVal > peer2MetricVal) ? 1 : -1;
            }
        });

        for (Map.Entry<String, ConcurrentHashMap<Enum<?>, AtomicDouble>> peer : metricMap.entrySet()) {
            if (peer.getValue().containsKey(metricName)) {
                AtomicDouble metricVal = peer.getValue().get(metricName);
                String[] peerMetricArr = {String.valueOf(metricName), String.valueOf(metricVal)};

                // Add tuple to PQ
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