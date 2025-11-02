package org.example.graph.topo;

import org.example.graph.common.Graph;
import org.example.graph.common.Metrics;

import java.util.*;

/**
 * Kahn's algorithm for topological sorting of a DAG.
 * Time complexity: O(V + E)
 * Space complexity: O(V)
 */
public class KahnTopologicalSort {
    private final Graph graph;
    private final Metrics metrics;

    /**
     * Constructs KahnTopologicalSort with a graph and metrics tracker.
     * @param graph the input directed acyclic graph
     * @param metrics metrics tracker
     */
    public KahnTopologicalSort(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Computes a topological ordering of the graph.
     * @return list of vertices in topological order, or null if graph has a cycle
     */
    public List<Integer> sort() {
        int n = graph.getVertexCount();
        int[] inDegree = new int[n];

        // Calculate in-degrees
        for (int u = 0; u < n; u++) {
            for (Graph.Edge edge : graph.getAdjacentEdges(u)) {
                inDegree[edge.to]++;
            }
        }

        // Initialize queue with vertices having 0 in-degree
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
                metrics.incrementCounter("pushes");
            }
        }

        List<Integer> topoOrder = new ArrayList<>();

        metrics.startTimer();

        // Process vertices in topological order
        while (!queue.isEmpty()) {
            int u = queue.poll();
            metrics.incrementCounter("pops");
            topoOrder.add(u);

            // Reduce in-degree of neighbors
            for (Graph.Edge edge : graph.getAdjacentEdges(u)) {
                int v = edge.to;
                inDegree[v]--;

                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.incrementCounter("pushes");
                }
            }
        }

        metrics.stopTimer();

        // Check if all vertices were processed (DAG check)
        if (topoOrder.size() != n) {
            return null; // Graph has a cycle
        }

        return topoOrder;
    }

    /**
     * Gets metrics for this execution.
     * @return metrics object
     */
    public Metrics getMetrics() {
        return metrics;
    }
}