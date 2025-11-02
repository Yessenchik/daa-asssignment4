package org.example.graph.scc;

import org.example.graph.common.Graph;
import org.example.graph.common.Metrics;

import java.util.*;

/**
 * Tarjan's algorithm for finding Strongly Connected Components (SCCs).
 * Time complexity: O(V + E)
 * Space complexity: O(V)
 */
public class TarjanSCC {
    private final Graph graph;
    private final Metrics metrics;

    private int[] ids;        // node id (discovery time)
    private int[] low;        // lowest id reachable
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int id;

    private List<List<Integer>> sccs;
    private int[] sccId;      // maps each vertex to its SCC id

    /**
     * Constructs TarjanSCC with a graph and metrics tracker.
     * @param graph the input directed graph
     * @param metrics metrics tracker
     */
    public TarjanSCC(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC requires directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Finds all strongly connected components.
     * @return list of SCCs, where each SCC is a list of vertex IDs
     */
    public List<List<Integer>> findSCCs() {
        int n = graph.getVertexCount();

        // Initialize data structures
        ids = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        sccs = new ArrayList<>();
        sccId = new int[n];
        Arrays.fill(ids, -1);
        Arrays.fill(sccId, -1);
        id = 0;

        metrics.startTimer();

        // Run DFS from every unvisited node
        for (int i = 0; i < n; i++) {
            if (ids[i] == -1) {
                dfs(i);
            }
        }

        metrics.stopTimer();

        return sccs;
    }

    /**
     * DFS traversal for Tarjan's algorithm.
     * @param u current vertex
     */
    private void dfs(int u) {
        // Assign discovery time and low-link value
        ids[u] = low[u] = id++;
        stack.push(u);
        onStack[u] = true;

        metrics.incrementCounter("dfs_visits");

        // Visit all neighbors
        for (Graph.Edge edge : graph.getAdjacentEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edges_explored");

            if (ids[v] == -1) {
                // Unvisited neighbor - continue DFS
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // Neighbor is on stack - part of current SCC
                low[u] = Math.min(low[u], ids[v]);
            }
        }

        // If u is a root node, pop the stack to form an SCC
        if (ids[u] == low[u]) {
            List<Integer> scc = new ArrayList<>();
            int sccIndex = sccs.size();

            while (true) {
                int v = stack.pop();
                onStack[v] = false;
                scc.add(v);
                sccId[v] = sccIndex;

                if (v == u) break;
            }

            // Sort SCC for consistent output
            Collections.sort(scc);
            sccs.add(scc);
            metrics.incrementCounter("sccs_found");
        }
    }

    /**
     * Gets the SCC ID for a vertex (must call findSCCs first).
     * @param vertex the vertex
     * @return SCC ID
     */
    public int getSccId(int vertex) {
        if (sccId == null) {
            throw new IllegalStateException("Must call findSCCs() first");
        }
        return sccId[vertex];
    }

    /**
     * Builds a condensation graph (DAG of SCCs).
     * Each SCC becomes a single node in the condensation graph.
     * @return condensation graph
     */
    public Graph buildCondensationGraph() {
        if (sccs == null) {
            throw new IllegalStateException("Must call findSCCs() first");
        }

        int numSccs = sccs.size();
        Graph condensation = new Graph(numSccs, true, graph.getWeightModel());
        Set<String> addedEdges = new HashSet<>();

        // For each edge in original graph
        for (int u = 0; u < graph.getVertexCount(); u++) {
            int sccU = sccId[u];

            for (Graph.Edge edge : graph.getAdjacentEdges(u)) {
                int v = edge.to;
                int sccV = sccId[v];

                // Add edge between different SCCs (avoid duplicates)
                if (sccU != sccV) {
                    String edgeKey = sccU + "->" + sccV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(sccU, sccV, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }

    /**
     * Gets metrics for this execution.
     * @return metrics object
     */
    public Metrics getMetrics() {
        return metrics;
    }
}