package org.example.graph.common;

import java.util.*;

/**
 * Represents a directed weighted graph.
 * Supports adjacency list representation with edge weights.
 */
public class Graph {
    private final int n; // number of vertices
    private final List<List<Edge>> adj; // adjacency list
    private final boolean directed;
    private final String weightModel; // "edge" or "node"

    /**
     * Edge class representing a weighted edge.
     */
    public static class Edge {
        public final int to;
        public final int weight;

        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "(" + to + ", w=" + weight + ")";
        }
    }

    /**
     * Constructs a graph with n vertices.
     * @param n number of vertices
     * @param directed whether the graph is directed
     * @param weightModel weight model ("edge" or "node")
     */
    public Graph(int n, boolean directed, String weightModel) {
        this.n = n;
        this.directed = directed;
        this.weightModel = weightModel;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    /**
     * Adds an edge to the graph.
     * @param u source vertex
     * @param v destination vertex
     * @param weight edge weight
     */
    public void addEdge(int u, int v, int weight) {
        adj.get(u).add(new Edge(v, weight));
        if (!directed) {
            adj.get(v).add(new Edge(u, weight));
        }
    }

    /**
     * Gets the number of vertices.
     * @return number of vertices
     */
    public int getVertexCount() {
        return n;
    }

    /**
     * Gets the adjacency list for a vertex.
     * @param u vertex
     * @return list of outgoing edges
     */
    public List<Edge> getAdjacentEdges(int u) {
        return adj.get(u);
    }

    /**
     * Checks if the graph is directed.
     * @return true if directed
     */
    public boolean isDirected() {
        return directed;
    }

    /**
     * Gets the weight model.
     * @return weight model string
     */
    public String getWeightModel() {
        return weightModel;
    }

    /**
     * Counts the total number of edges.
     * @return number of edges
     */
    public int getEdgeCount() {
        int count = 0;
        for (int i = 0; i < n; i++) {
            count += adj.get(i).size();
        }
        return directed ? count : count / 2;
    }

    /**
     * Creates a transpose (reverse) of this graph.
     * @return transposed graph
     */
    public Graph transpose() {
        if (!directed) {
            throw new UnsupportedOperationException("Cannot transpose undirected graph");
        }
        Graph transposed = new Graph(n, directed, weightModel);
        for (int u = 0; u < n; u++) {
            for (Edge e : adj.get(u)) {
                transposed.addEdge(e.to, u, e.weight);
            }
        }
        return transposed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph (n=").append(n).append(", edges=").append(getEdgeCount()).append("):\n");
        for (int i = 0; i < n; i++) {
            sb.append("  ").append(i).append(" -> ").append(adj.get(i)).append("\n");
        }
        return sb.toString();
    }
}