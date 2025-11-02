package org.example.graph;

import org.example.graph.common.Graph;
import org.example.graph.common.MetricsImpl;
import org.example.graph.scc.TarjanSCC;
import org.example.graph.topo.KahnTopologicalSort;
import org.example.graph.topo.DFSTopologicalSort;
import org.example.graph.dagsp.DAGShortestPath;
import org.example.graph.util.GraphLoader;

import java.io.File;
import java.util.List;

/**
 * Main application to run all graph algorithms on test datasets.
 * Generates results for analysis and reporting.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("GRAPH ALGORITHMS - ASSIGNMENT 4");
        System.out.println("Smart City / Smart Campus Scheduling");
        System.out.println("=".repeat(80));
        System.out.println();

        File dataDir = new File("data");
        if (!dataDir.exists()) {
            System.err.println("Error: data directory not found");
            return;
        }

        File[] dataFiles = dataDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (dataFiles == null || dataFiles.length == 0) {
            System.err.println("Error: no JSON files found in data directory");
            return;
        }

        for (File file : dataFiles) {
            processDataset(file.getPath());
            System.out.println();
        }

        System.out.println("=".repeat(80));
        System.out.println("Analysis complete. See README.md for full report.");
        System.out.println("=".repeat(80));
    }

    private static void processDataset(String filepath) {
        System.out.println("-".repeat(80));
        System.out.println("Dataset: " + new File(filepath).getName());
        System.out.println("-".repeat(80));

        try {
            // Load graph
            GraphLoader.GraphData graphData = GraphLoader.loadFromJson(filepath);
            Graph graph = graphData.getGraph();
            int source = graphData.getSource();

            System.out.println("Graph Info:");
            System.out.println("  Nodes: " + graph.getVertexCount());
            System.out.println("  Edges: " + graph.getEdgeCount());
            System.out.println("  Directed: " + graph.isDirected());
            System.out.println("  Weight Model: " + graph.getWeightModel());
            System.out.println("  Source Node: " + source);
            System.out.println();

            // Run SCC analysis
            runSCCAnalysis(graph);

            // Run Topological Sort
            runTopologicalSort(graph);

            // Run DAG Shortest/Longest Paths if applicable
            runDAGPathAnalysis(graph, source);

        } catch (Exception e) {
            System.err.println("Error processing dataset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runSCCAnalysis(Graph graph) {
        System.out.println("### Strongly Connected Components (Tarjan's Algorithm) ###");

        MetricsImpl metrics = new MetricsImpl();
        TarjanSCC scc = new TarjanSCC(graph, metrics);

        List<List<Integer>> sccs = scc.findSCCs();

        System.out.println("Results:");
        System.out.println("  Number of SCCs: " + sccs.size());
        System.out.println("  SCC Details:");
        for (int i = 0; i < sccs.size(); i++) {
            List<Integer> component = sccs.get(i);
            System.out.println("    SCC " + i + " (size " + component.size() + "): " + component);
        }

        // Build condensation graph
        Graph condensation = scc.buildCondensationGraph();
        System.out.println("  Condensation Graph:");
        System.out.println("    Nodes: " + condensation.getVertexCount());
        System.out.println("    Edges: " + condensation.getEdgeCount());

        System.out.println("Metrics:");
        System.out.println("  " + metrics.getSummary().replace("\n", "\n  ").trim());
        System.out.println();
    }

    private static void runTopologicalSort(Graph graph) {
        System.out.println("### Topological Sort ###");

        // Try Kahn's algorithm
        System.out.println("Kahn's Algorithm:");
        MetricsImpl kahnMetrics = new MetricsImpl();
        KahnTopologicalSort kahnSort = new KahnTopologicalSort(graph, kahnMetrics);
        List<Integer> kahnResult = kahnSort.sort();

        if (kahnResult != null) {
            System.out.println("  Topological Order: " + kahnResult);
            System.out.println("  Metrics:");
            System.out.println("    " + kahnMetrics.getSummary().replace("\n", "\n    ").trim());
        } else {
            System.out.println("  Result: Graph contains a cycle (not a DAG)");
        }

        // Try DFS algorithm
        System.out.println("DFS Algorithm:");
        MetricsImpl dfsMetrics = new MetricsImpl();
        DFSTopologicalSort dfsSort = new DFSTopologicalSort(graph, dfsMetrics);
        List<Integer> dfsResult = dfsSort.sort();

        if (dfsResult != null) {
            System.out.println("  Topological Order: " + dfsResult);
            System.out.println("  Metrics:");
            System.out.println("    " + dfsMetrics.getSummary().replace("\n", "\n    ").trim());
        } else {
            System.out.println("  Result: Graph contains a cycle (not a DAG)");
        }
        System.out.println();
    }

    private static void runDAGPathAnalysis(Graph graph, int source) {
        System.out.println("### DAG Shortest/Longest Paths ###");

        // Check if it's a DAG first
        MetricsImpl topoMetrics = new MetricsImpl();
        KahnTopologicalSort topoSort = new KahnTopologicalSort(graph, topoMetrics);
        if (topoSort.sort() == null) {
            System.out.println("  Skipped: Graph contains cycles (not a DAG)");
            System.out.println();
            return;
        }

        // Shortest paths
        System.out.println("Shortest Paths from source " + source + ":");
        MetricsImpl shortestMetrics = new MetricsImpl();
        DAGShortestPath dagSP = new DAGShortestPath(graph, shortestMetrics);
        DAGShortestPath.PathResult shortestResult = dagSP.shortestPaths(source);

        System.out.println("  Distances:");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (shortestResult.dist[i] != Integer.MAX_VALUE) {
                List<Integer> path = shortestResult.reconstructPath(i);
                System.out.println("    To " + i + ": " + shortestResult.dist[i] +
                        " (path: " + path + ")");
            }
        }
        System.out.println("  Metrics:");
        System.out.println("    " + shortestMetrics.getSummary().replace("\n", "\n    ").trim());

        // Longest paths
        System.out.println("Longest Paths from source " + source + ":");
        MetricsImpl longestMetrics = new MetricsImpl();
        DAGShortestPath dagLP = new DAGShortestPath(graph, longestMetrics);
        DAGShortestPath.PathResult longestResult = dagLP.longestPaths(source);

        System.out.println("  Distances:");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (longestResult.dist[i] != Integer.MIN_VALUE) {
                List<Integer> path = longestResult.reconstructPath(i);
                System.out.println("    To " + i + ": " + longestResult.dist[i] +
                        " (path: " + path + ")");
            }
        }
        System.out.println("  Metrics:");
        System.out.println("    " + longestMetrics.getSummary().replace("\n", "\n    ").trim());

        // Critical path
        System.out.println("Critical Path (Longest Path in entire DAG):");
        DAGShortestPath.CriticalPathResult critical = dagLP.findCriticalPath();
        if (critical != null) {
            System.out.println("  Path: " + critical.path);
            System.out.println("  Length: " + critical.length);
        }

        System.out.println();
    }
}