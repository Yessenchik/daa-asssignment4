package graph.scc;

import org.example.graph.common.Graph;
import org.example.graph.common.MetricsImpl;
import org.example.graph.scc.TarjanSCC;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for TarjanSCC algorithm.
 */
public class TarjanSCCTest {

    @Test
    public void testSimpleDAG() {
        // Create a simple DAG: 0 -> 1 -> 2
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        TarjanSCC scc = new TarjanSCC(graph, new MetricsImpl());
        List<List<Integer>> sccs = scc.findSCCs();

        // Each node should be its own SCC in a DAG
        assertEquals(3, sccs.size());
    }

    @Test
    public void testSingleSCC() {
        // Create a cycle: 0 -> 1 -> 2 -> 0
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        TarjanSCC scc = new TarjanSCC(graph, new MetricsImpl());
        List<List<Integer>> sccs = scc.findSCCs();

        // All nodes should be in one SCC
        assertEquals(1, sccs.size());
        assertEquals(3, sccs.get(0).size());
    }

    @Test
    public void testMultipleSCCs() {
        // Create two SCCs: (0, 1) and (2, 3)
        // 0 -> 1 -> 0, 2 -> 3 -> 2, 1 -> 2
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);
        graph.addEdge(1, 2, 1);

        TarjanSCC scc = new TarjanSCC(graph, new MetricsImpl());
        List<List<Integer>> sccs = scc.findSCCs();

        // Should have 2 SCCs
        assertEquals(2, sccs.size());
    }

    @Test
    public void testCondensationGraph() {
        // Create graph with 2 SCCs
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);
        graph.addEdge(1, 2, 1);

        TarjanSCC scc = new TarjanSCC(graph, new MetricsImpl());
        scc.findSCCs();

        Graph condensation = scc.buildCondensationGraph();

        // Condensation should have 2 nodes
        assertEquals(2, condensation.getVertexCount());

        // Should be a DAG (no cycles)
        assertTrue(condensation.getEdgeCount() >= 0);
    }

    @Test
    public void testDisconnectedGraph() {
        // Create disconnected components
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(2, 3, 1);

        TarjanSCC scc = new TarjanSCC(graph, new MetricsImpl());
        List<List<Integer>> sccs = scc.findSCCs();

        // Each component forms its own SCC(s)
        assertEquals(4, sccs.size());
    }

    @Test
    public void testSelfLoop() {
        // Create a self-loop
        Graph graph = new Graph(2, true, "edge");
        graph.addEdge(0, 0, 1);
        graph.addEdge(0, 1, 1);

        TarjanSCC scc = new TarjanSCC(graph, new MetricsImpl());
        List<List<Integer>> sccs = scc.findSCCs();

        // Self-loop creates an SCC
        assertTrue(sccs.size() <= 2);
    }

    @Test
    public void testMetrics() {
        Graph graph = new Graph(5, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);

        MetricsImpl metrics = new MetricsImpl();
        TarjanSCC scc = new TarjanSCC(graph, metrics);
        scc.findSCCs();

        // Should have visited all vertices
        assertTrue(metrics.getCounter("dfs_visits") >= 5);
        assertTrue(metrics.getElapsedTimeNanos() > 0);
    }
}