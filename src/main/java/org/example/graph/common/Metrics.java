package org.example.graph.common;

/**
 * Common Metrics interface for tracking algorithm performance.
 * Provides counters for operations and timing measurements.
 */
public interface Metrics {
    /**
     * Starts the timer for measuring execution time.
     */
    void startTimer();

    /**
     * Stops the timer and records the elapsed time.
     */
    void stopTimer();

    /**
     * Gets the elapsed time in nanoseconds.
     * @return elapsed time in nanoseconds
     */
    long getElapsedTimeNanos();

    /**
     * Gets the elapsed time in milliseconds.
     * @return elapsed time in milliseconds
     */
    double getElapsedTimeMillis();

    /**
     * Increments a named counter.
     * @param counterName name of the counter
     */
    void incrementCounter(String counterName);

    /**
     * Increments a named counter by a specific amount.
     * @param counterName name of the counter
     * @param amount amount to increment
     */
    void incrementCounter(String counterName, int amount);

    /**
     * Gets the value of a named counter.
     * @param counterName name of the counter
     * @return counter value
     */
    long getCounter(String counterName);

    /**
     * Resets all metrics.
     */
    void reset();

    /**
     * Returns a string representation of all metrics.
     * @return formatted metrics string
     */
    String getSummary();
}