package graph.topo;

import org.example.graph.common.Graph;
import org.example.graph.common.MetricsImpl;
import org.example.graph.topo.DFSTopologicalSort;
import org.example.graph.topo.KahnTopologicalSort;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for Topological Sort algorithms.
 */
public class TopologicalSortTest {

    @Test
    public void testKahnSimpleDAG() {
        // Create a simple DAG: 0 -> 1 -> 2
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        KahnTopologicalSort topoSort = new KahnTopologicalSort(graph, new MetricsImpl());
        List<Integer> result = topoSort.sort();

        assertNotNull(result);
        assertEquals(3, result.size());

        // 0 should come before 1, 1 before 2
        assertTrue(result.indexOf(0) < result.indexOf(1));
        assertTrue(result.indexOf(1) < result.indexOf(2));
    }

    @Test
    public void testDFSSimpleDAG() {
        // Create a simple DAG: 0 -> 1 -> 2
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        DFSTopologicalSort topoSort = new DFSTopologicalSort(graph, new MetricsImpl());
        List<Integer> result = topoSort.sort();

        assertNotNull(result);
        assertEquals(3, result.size());

        // 0 should come before 1, 1 before 2
        assertTrue(result.indexOf(0) < result.indexOf(1));
        assertTrue(result.indexOf(1) < result.indexOf(2));
    }

    @Test
    public void testKahnWithCycle() {
        // Create a cycle: 0 -> 1 -> 2 -> 0
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        KahnTopologicalSort topoSort = new KahnTopologicalSort(graph, new MetricsImpl());
        List<Integer> result = topoSort.sort();

        // Should return null for cyclic graph
        assertNull(result);
    }

    @Test
    public void testDFSWithCycle() {
        // Create a cycle: 0 -> 1 -> 2 -> 0
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        DFSTopologicalSort topoSort = new DFSTopologicalSort(graph, new MetricsImpl());
        List<Integer> result = topoSort.sort();

        // Should return null for cyclic graph
        assertNull(result);
    }

    @Test
    public void testComplexDAG() {
        // Create a diamond DAG: 0 -> 1,2 -> 3
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);

        KahnTopologicalSort topoSort = new KahnTopologicalSort(graph, new MetricsImpl());
        List<Integer> result = topoSort.sort();

        assertNotNull(result);
        assertEquals(4, result.size());

        // 0 must come before all others
        assertEquals(0, result.get(0).intValue());
        // 3 must come last
        assertEquals(3, result.get(3).intValue());
    }

    @Test
    public void testDisconnectedDAG() {
        // Create disconnected DAG
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(2, 3, 1);

        KahnTopologicalSort topoSort = new KahnTopologicalSort(graph, new MetricsImpl());
        List<Integer> result = topoSort.sort();

        assertNotNull(result);
        assertEquals(4, result.size());

        // 0 before 1, 2 before 3
        assertTrue(result.indexOf(0) < result.indexOf(1));
        assertTrue(result.indexOf(2) < result.indexOf(3));
    }

    @Test
    public void testKahnMetrics() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        MetricsImpl metrics = new MetricsImpl();
        KahnTopologicalSort topoSort = new KahnTopologicalSort(graph, metrics);
        topoSort.sort();

        // Should have tracked operations
        assertTrue(metrics.getCounter("pushes") > 0);
        assertTrue(metrics.getCounter("pops") > 0);
        assertTrue(metrics.getElapsedTimeNanos() > 0);
    }

    @Test
    public void testDFSMetrics() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        MetricsImpl metrics = new MetricsImpl();
        DFSTopologicalSort topoSort = new DFSTopologicalSort(graph, metrics);
        topoSort.sort();

        // Should have tracked operations
        assertTrue(metrics.getCounter("dfs_visits") >= 3);
        assertTrue(metrics.getCounter("edges_explored") >= 2);
        assertTrue(metrics.getElapsedTimeNanos() > 0);
    }
}