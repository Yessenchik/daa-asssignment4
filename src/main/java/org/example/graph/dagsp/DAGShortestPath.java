package org.example.graph.dagsp;

import org.example.graph.common.Graph;
import org.example.graph.common.Metrics;
import org.example.graph.topo.KahnTopologicalSort;

import java.util.*;

/**
 * Shortest and longest path algorithms for Directed Acyclic Graphs (DAGs).
 * Uses dynamic programming over topological order.
 * Time complexity: O(V + E)
 */
public class DAGShortestPath {
    private final Graph graph;
    private final Metrics metrics;

    /**
     * Constructs DAGShortestPath with a graph and metrics tracker.
     * @param graph the input DAG
     * @param metrics metrics tracker
     */
    public DAGShortestPath(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("DAG shortest path requires directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Result class containing distances and parent pointers.
     */
    public static class PathResult {
        public final int[] dist;
        public final int[] parent;
        public final int source;

        public PathResult(int[] dist, int[] parent, int source) {
            this.dist = dist;
            this.parent = parent;
            this.source = source;
        }

        /**
         * Reconstructs the path from source to destination.
         * @param dest destination vertex
         * @return list of vertices in the path, or null if no path exists
         */
        public List<Integer> reconstructPath(int dest) {
            if (dist[dest] == Integer.MAX_VALUE || dist[dest] == Integer.MIN_VALUE) {
                return null; // No path
            }

            List<Integer> path = new ArrayList<>();
            for (int v = dest; v != -1; v = parent[v]) {
                path.add(v);
            }
            Collections.reverse(path);
            return path;
        }
    }

    /**
     * Computes shortest paths from a source vertex to all other vertices.
     * @param source source vertex
     * @return PathResult containing distances and parents
     */
    public PathResult shortestPaths(int source) {
        int n = graph.getVertexCount();

        // Get topological order
        KahnTopologicalSort topoSort = new KahnTopologicalSort(graph, new org.example.graph.common.MetricsImpl());
        List<Integer> topoOrder = topoSort.sort();

        if (topoOrder == null) {
            throw new IllegalArgumentException("Graph contains a cycle - not a DAG");
        }

        // Initialize distances and parents
        int[] dist = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        metrics.startTimer();

        // Process vertices in topological order
        for (int u : topoOrder) {
            if (dist[u] != Integer.MAX_VALUE) {
                for (Graph.Edge edge : graph.getAdjacentEdges(u)) {
                    int v = edge.to;
                    int newDist = dist[u] + edge.weight;

                    metrics.incrementCounter("relaxations");

                    if (newDist < dist[v]) {
                        dist[v] = newDist;
                        parent[v] = u;
                    }
                }
            }
        }

        metrics.stopTimer();

        return new PathResult(dist, parent, source);
    }

    /**
     * Computes longest paths from a source vertex to all other vertices.
     * Uses negated weights to find longest path.
     * @param source source vertex
     * @return PathResult containing distances and parents
     */
    public PathResult longestPaths(int source) {
        int n = graph.getVertexCount();

        // Get topological order
        KahnTopologicalSort topoSort = new KahnTopologicalSort(graph, new org.example.graph.common.MetricsImpl());
        List<Integer> topoOrder = topoSort.sort();

        if (topoOrder == null) {
            throw new IllegalArgumentException("Graph contains a cycle - not a DAG");
        }

        // Initialize distances and parents
        int[] dist = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Integer.MIN_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        metrics.startTimer();

        // Process vertices in topological order (maximize distance)
        for (int u : topoOrder) {
            if (dist[u] != Integer.MIN_VALUE) {
                for (Graph.Edge edge : graph.getAdjacentEdges(u)) {
                    int v = edge.to;
                    int newDist = dist[u] + edge.weight;

                    metrics.incrementCounter("relaxations");

                    if (newDist > dist[v]) {
                        dist[v] = newDist;
                        parent[v] = u;
                    }
                }
            }
        }

        metrics.stopTimer();

        return new PathResult(dist, parent, source);
    }

    /**
     * Finds the critical path (longest path) in the entire DAG.
     * @return PathResult for the critical path
     */
    public CriticalPathResult findCriticalPath() {
        int n = graph.getVertexCount();
        int maxDist = Integer.MIN_VALUE;
        int criticalEnd = -1;
        PathResult bestResult = null;

        // Try each vertex as a potential start of critical path
        for (int source = 0; source < n; source++) {
            PathResult result = longestPaths(source);

            for (int dest = 0; dest < n; dest++) {
                if (result.dist[dest] != Integer.MIN_VALUE && result.dist[dest] > maxDist) {
                    maxDist = result.dist[dest];
                    criticalEnd = dest;
                    bestResult = result;
                }
            }
        }

        if (bestResult == null) {
            return null;
        }

        List<Integer> criticalPath = bestResult.reconstructPath(criticalEnd);
        return new CriticalPathResult(criticalPath, maxDist);
    }

    /**
     * Result class for critical path.
     */
    public static class CriticalPathResult {
        public final List<Integer> path;
        public final int length;

        public CriticalPathResult(List<Integer> path, int length) {
            this.path = path;
            this.length = length;
        }
    }

    /**
     * Gets metrics for this execution.
     * @return metrics object
     */
    public Metrics getMetrics() {
        return metrics;
    }
}