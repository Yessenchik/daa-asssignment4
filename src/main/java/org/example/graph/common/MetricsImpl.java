package org.example.graph.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Metrics interface.
 * Tracks timing and operation counters for algorithm performance analysis.
 */
public class MetricsImpl implements Metrics {
    private long startTime;
    private long endTime;
    private final Map<String, Long> counters;

    public MetricsImpl() {
        this.counters = new HashMap<>();
        reset();
    }

    @Override
    public void startTimer() {
        startTime = System.nanoTime();
    }

    @Override
    public void stopTimer() {
        endTime = System.nanoTime();
    }

    @Override
    public long getElapsedTimeNanos() {
        return endTime - startTime;
    }

    @Override
    public double getElapsedTimeMillis() {
        return getElapsedTimeNanos() / 1_000_000.0;
    }

    @Override
    public void incrementCounter(String counterName) {
        incrementCounter(counterName, 1);
    }

    @Override
    public void incrementCounter(String counterName, int amount) {
        counters.put(counterName, counters.getOrDefault(counterName, 0L) + amount);
    }

    @Override
    public long getCounter(String counterName) {
        return counters.getOrDefault(counterName, 0L);
    }

    @Override
    public void reset() {
        startTime = 0;
        endTime = 0;
        counters.clear();
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Execution Time: ").append(String.format("%.3f", getElapsedTimeMillis())).append(" ms\n");
        sb.append("Counters:\n");
        for (Map.Entry<String, Long> entry : counters.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}