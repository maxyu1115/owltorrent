package edu.rice.owltorrent.network;

import com.google.common.util.concurrent.AtomicDouble;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

public class MeteringSystemTest {

    @Test
    public void addMetric() {
        MeteringSystem meteringSystem = new MeteringSystem();
        String peerID = "peerABC";


    }

    @Test
    public void getMetric() {
        MeteringSystem meteringSystem = new MeteringSystem();
        TwentyByteId peerID = TwentyByteId.fromString("12345678901234567890");

        // Populate inner map
        ConcurrentHashMap<Enum<?>, AtomicDouble> peerMetrics = new ConcurrentHashMap<>();
        MeteringSystem.Metrics metric = MeteringSystem.Metrics.EFFECTIVE_PIECES;
        AtomicDouble metricVal = new AtomicDouble(10);
        peerMetrics.put(metric, metricVal);

        // Populate outer map
        meteringSystem.metricMap.put(peerID, peerMetrics);

        Assert.assertEquals(meteringSystem.getMetric(peerID, metric), metricVal);
    }

    @Test
    public void rankPeersByMetric() {
    }
}