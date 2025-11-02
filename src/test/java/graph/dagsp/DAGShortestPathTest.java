package graph.dagsp;

import org.example.graph.common.Graph;
import org.example.graph.common.MetricsImpl;
import org.example.graph.dagsp.DAGShortestPath;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for DAG Shortest/Longest Path algorithms.
 */
public class DAGShortestPathTest {

    @Test
    public void testShortestPathSimple() {
        // Create a simple path: 0 -> 1 -> 2
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 3);

        DAGShortestPath dagSP = new DAGShortestPath(graph, new MetricsImpl());
        DAGShortestPath.PathResult result = dagSP.shortestPaths(0);

        assertEquals(0, result.dist[0]);
        assertEquals(5, result.dist[1]);
        assertEquals(8, result.dist[2]);
    }

    @Test
    public void testShortestPathDiamond() {
        // Create diamond: 0 -> 1,2 -> 3
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 4);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 1);

        DAGShortestPath dagSP = new DAGShortestPath(graph, new MetricsImpl());
        DAGShortestPath.PathResult result = dagSP.shortestPaths(0);

        assertEquals(0, result.dist[0]);
        assertEquals(1, result.dist[1]);
        assertEquals(4, result.dist[2]);
        assertEquals(3, result.dist[3]); // via 0->1->3
    }

    @Test
    public void testLongestPathSimple() {
        // Create a simple path: 0 -> 1 -> 2
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 5);
        graph.addEdge(1, 2, 3);

        DAGShortestPath dagSP = new DAGShortestPath(graph, new MetricsImpl());
        DAGShortestPath.PathResult result = dagSP.longestPaths(0);

        assertEquals(0, result.dist[0]);
        assertEquals(5, result.dist[1]);
        assertEquals(8, result.dist[2]);
    }

    @Test
    public void testLongestPathDiamond() {
        // Create diamond: 0 -> 1,2 -> 3
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 4);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 1);

        DAGShortestPath dagSP = new DAGShortestPath(graph, new MetricsImpl());
        DAGShortestPath.PathResult result = dagSP.longestPaths(0);

        assertEquals(0, result.dist[0]);
        assertEquals(1, result.dist[1]);
        assertEquals(4, result.dist[2]);
        assertEquals(5, result.dist[3]); // via 0->2->3
    }

    @Test
    public void testPathReconstruction() {
        // Create a simple path: 0 -> 1 -> 2 -> 3
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 3, 3);

        DAGShortestPath dagSP = new DAGShortestPath(graph, new MetricsImpl());
        DAGShortestPath.PathResult result = dagSP.shortestPaths(0);

        List<Integer> path = result.reconstructPath(3);
        assertNotNull(path);
        assertEquals(4, path.size());
        assertEquals(0, path.get(0).intValue());
        assertEquals(1, path.get(1).intValue());
        assertEquals(2, path.get(2).intValue());
        assertEquals(3, path.get(3).intValue());
    }

    @Test
    public void testUnreachableVertex() {
        // Create disconnected graph
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(2, 3, 1);

        DAGShortestPath dagSP = new DAGShortestPath(graph, new MetricsImpl());
        DAGShortestPath.PathResult result = dagSP.shortestPaths(0);

        // Vertex 2 and 3 should be unreachable from 0
        assertEquals(Integer.MAX_VALUE, result.dist[2]);
        assertEquals(Integer.MAX_VALUE, result.dist[3]);

        List<Integer> path = result.reconstructPath(2);
        assertNull(path);
    }

    @Test
    public void testCriticalPath() {
        // Create a graph with multiple paths
        Graph graph = new Graph(5, true, "edge");
        graph.addEdge(0, 1, 3);
        graph.addEdge(0, 2, 2);
        graph.addEdge(1, 3, 4);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 5);

        DAGShortestPath dagSP = new DAGShortestPath(graph, new MetricsImpl());
        DAGShortestPath.CriticalPathResult critical = dagSP.findCriticalPath();

        assertNotNull(critical);
        assertNotNull(critical.path);
        assertTrue(critical.length > 0);

        // Path should start with 0 and end with 4
        assertEquals(0, critical.path.get(0).intValue());
        assertEquals(4, critical.path.get(critical.path.size() - 1).intValue());
    }

    @Test
    public void testMetrics() {
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 3, 3);

        MetricsImpl metrics = new MetricsImpl();
        DAGShortestPath dagSP = new DAGShortestPath(graph, metrics);
        dagSP.shortestPaths(0);

        // Should have tracked relaxations
        assertTrue(metrics.getCounter("relaxations") >= 3);
        assertTrue(metrics.getElapsedTimeNanos() > 0);
    }

    @Test
    public void testCycleDetection() {
        // Create a cycle: 0 -> 1 -> 2 -> 0
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 0, 3);

        DAGShortestPath dagSP = new DAGShortestPath(graph, new MetricsImpl());

        // Should throw exception or handle cycle
        assertThrows(IllegalArgumentException.class, () -> {
            dagSP.shortestPaths(0);
        });
    }
}