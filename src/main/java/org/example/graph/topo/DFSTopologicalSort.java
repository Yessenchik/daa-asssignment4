package org.example.graph.topo;

import org.example.graph.common.Graph;
import org.example.graph.common.Metrics;

import java.util.*;

/**
 * DFS-based algorithm for topological sorting of a DAG.
 * Time complexity: O(V + E)
 * Space complexity: O(V)
 */
public class DFSTopologicalSort {
    private final Graph graph;
    private final Metrics metrics;

    private boolean[] visited;
    private boolean[] recStack;
    private Stack<Integer> stack;
    private boolean hasCycle;

    /**
     * Constructs DFSTopologicalSort with a graph and metrics tracker.
     * @param graph the input directed acyclic graph
     * @param metrics metrics tracker
     */
    public DFSTopologicalSort(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Computes a topological ordering of the graph using DFS.
     * @return list of vertices in topological order, or null if graph has a cycle
     */
    public List<Integer> sort() {
        int n = graph.getVertexCount();
        visited = new boolean[n];
        recStack = new boolean[n];
        stack = new Stack<>();
        hasCycle = false;

        metrics.startTimer();

        // Run DFS from each unvisited vertex
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfs(i);
                if (hasCycle) {
                    metrics.stopTimer();
                    return null; // Cycle detected
                }
            }
        }

        metrics.stopTimer();

        // Build topological order from stack
        List<Integer> topoOrder = new ArrayList<>();
        while (!stack.isEmpty()) {
            topoOrder.add(stack.pop());
        }

        return topoOrder;
    }

    /**
     * DFS traversal for topological sorting.
     * @param u current vertex
     */
    private void dfs(int u) {
        visited[u] = true;
        recStack[u] = true;
        metrics.incrementCounter("dfs_visits");

        for (Graph.Edge edge : graph.getAdjacentEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edges_explored");

            if (!visited[v]) {
                dfs(v);
            } else if (recStack[v]) {
                // Back edge found - cycle detected
                hasCycle = true;
                return;
            }
        }

        recStack[u] = false;
        stack.push(u);
    }

    /**
     * Gets metrics for this execution.
     * @return metrics object
     */
    public Metrics getMetrics() {
        return metrics;
    }
}