package org.example.graph.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Utility for generating test datasets for graph algorithms.
 * Not required for runtime but useful for dataset generation.
 */
public class DatasetGenerator {

    private static class GraphData {
        boolean directed;
        int n;
        List<Edge> edges;
        int source;
        String weight_model;

        static class Edge {
            int u, v, w;
            Edge(int u, int v, int w) {
                this.u = u;
                this.v = v;
                this.w = w;
            }
        }
    }

    /**
     * Generates a dataset and saves it to a file.
     */
    public static void generateDataset(String filename, int n, int edgeCount,
                                       boolean includesCycle, int numSCCs,
                                       String description) throws IOException {
        Random rand = new Random(42); // Fixed seed for reproducibility
        GraphData data = new GraphData();
        data.directed = true;
        data.n = n;
        data.edges = new ArrayList<>();
        data.source = 0;
        data.weight_model = "edge";

        Set<String> edgeSet = new HashSet<>();

        if (includesCycle) {
            // Generate graph with cycles
            if (numSCCs > 1) {
                // Multiple SCCs with cycles
                int nodesPerSCC = n / numSCCs;
                for (int scc = 0; scc < numSCCs; scc++) {
                    int start = scc * nodesPerSCC;
                    int end = (scc == numSCCs - 1) ? n : (scc + 1) * nodesPerSCC;

                    // Create cycle within SCC
                    for (int i = start; i < end - 1; i++) {
                        addEdge(data, edgeSet, i, i + 1, rand.nextInt(10) + 1);
                    }
                    if (end - start > 1) {
                        addEdge(data, edgeSet, end - 1, start, rand.nextInt(10) + 1);
                    }
                }

                // Add edges between SCCs
                for (int i = 0; i < numSCCs - 1; i++) {
                    int fromSCC = i * nodesPerSCC;
                    int toSCC = (i + 1) * nodesPerSCC;
                    addEdge(data, edgeSet, fromSCC + rand.nextInt(nodesPerSCC),
                            toSCC, rand.nextInt(10) + 1);
                }
            } else {
                // Single SCC with cycle
                for (int i = 0; i < Math.min(n, 4); i++) {
                    addEdge(data, edgeSet, i, (i + 1) % Math.min(n, 4), rand.nextInt(10) + 1);
                }
            }
        }

        // Add remaining edges
        int attempts = 0;
        while (data.edges.size() < edgeCount && attempts < edgeCount * 10) {
            int u = rand.nextInt(n);
            int v = rand.nextInt(n);
            if (u != v) {
                addEdge(data, edgeSet, u, v, rand.nextInt(10) + 1);
            }
            attempts++;
        }

        // Save to file
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("# " + description + "\n");
            writer.write(gson.toJson(data));
        }

        System.out.println("Generated: " + filename);
        System.out.println("  Nodes: " + n + ", Edges: " + data.edges.size());
    }

    private static void addEdge(GraphData data, Set<String> edgeSet, int u, int v, int w) {
        String key = u + "->" + v;
        if (!edgeSet.contains(key)) {
            data.edges.add(new GraphData.Edge(u, v, w));
            edgeSet.add(key);
        }
    }
}