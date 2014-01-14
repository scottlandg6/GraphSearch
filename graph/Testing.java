package graph;

import org.junit.Test;
import ucb.junit.textui;
import static org.junit.Assert.*;

/* You MAY add public @Test methods to this class.  You may also add
 * additional public classes containing "Testing" in their name. These
 * may not be part of your graph package per se (that is, it must be
 * possible to remove them and still have your package work). */

/** Unit tests for the graph package.
 *  @author Scott Lee
 */
public class Testing {

    /** Run all JUnit tests in the graph package. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(graph.Testing.class));
    }
    @Test
    public void emptyGraph() {
        DirectedGraph g = new DirectedGraph();
        assertEquals("Initial graph has vertices", 0, g.vertexSize());
        assertEquals("Initial graph has edges", 0, g.edgeSize());
    }

    @Test
    public void testDirectedaddvertex() {
        Graph<String, Integer> g = new DirectedGraph<String, Integer>();
        DirectedGraph<String, Integer>.Vertex one = g.add("one");
        DirectedGraph<String, Integer>.Vertex two = g.add("two");
        g.add(one, two, 4);
        assertEquals("Error in vertex size", 2, g.vertexSize());
        assertEquals("Error in edge size", 1, g.edgeSize());
        assertEquals("Error in outDegree", 1, g.outDegree(one));
        assertEquals("Error in outDegree", 0, g.outDegree(two));
        assertEquals("Error in inDegree", 0, g.inDegree(one));
        assertEquals("Error in inDegree", 1, g.inDegree(two));
    }

    @Test
    public void testIndegreeAndSizeUndirected() {
        Graph<String, Integer> graph = new UndirectedGraph<String, Integer>();
        UndirectedGraph<String, Integer>.Vertex A = graph.add("A");
        UndirectedGraph<String, Integer>.Vertex B = graph.add("B");
        UndirectedGraph<String, Integer>.Vertex C = graph.add("C");
        UndirectedGraph<String, Integer>.Vertex D = graph.add("D");
        UndirectedGraph<String, Integer>.Vertex E = graph.add("E");
        graph.add(C, A, 6);
        graph.add(B, D, 9);
        graph.add(A, D, 1);
        graph.add(E, A, 2);
        graph.add(E, C, 3);
        assertEquals("Error in degree undirected", 3, graph.degree(A));
        assertEquals("Error with contains without in undirected", true,
                graph.contains(A, C));
        assertEquals("Error with contains without in undirected", true,
                graph.contains(C, A));
        assertEquals("Error with contains in undirected", true,
                graph.contains(A, C, 6));
        assertEquals("Error with contains in undirected", true,
                graph.contains(C, A, 6));
        assertEquals("Error in vertex size", 5, graph.vertexSize());
        assertEquals("Error in edge size", 5, graph.edgeSize());
    }

    @Test
    public void testContainsUndirected() {
        Graph<String, Integer> g = new UndirectedGraph<String, Integer>();
        UndirectedGraph<String, Integer>.Vertex A = g.add("A");
        UndirectedGraph<String, Integer>.Vertex B = g.add("B");
        UndirectedGraph<String, Integer>.Vertex C = g.add("C");
        UndirectedGraph<String, Integer>.Vertex D = g.add("D");
        UndirectedGraph<String, Integer>.Vertex E = g.add("E");
        g.add(C, A, 1);
        g.add(E, A, 4);
        g.add(B, D, 2);
        g.add(E, C, 5);
        g.add(A, D, 3);
        assertEquals("Error with contains without a label", true,
                g.contains(A, C));
        assertEquals("Error with contains without a label", true,
                g.contains(C, A));
        assertEquals("Error with contains with a label", true,
                g.contains(A, C, 1));
        assertEquals("Error with contains with a label", true,
                g.contains(C, A, 1));
    }
    @Test
    public void testContainsDirected() {
        DirectedGraph<String, Integer> g = new DirectedGraph<String, Integer>();
        DirectedGraph<String, Integer>.Vertex A = g.add("A");
        DirectedGraph<String, Integer>.Vertex B = g.add("B");
        DirectedGraph<String, Integer>.Vertex C = g.add("C");
        DirectedGraph<String, Integer>.Vertex D = g.add("D");
        DirectedGraph<String, Integer>.Vertex E = g.add("E");
        g.add(C, A, 10);
        g.add(B, D, 4);
        g.add(A, D, 3);
        g.add(E, A, 2);
        g.add(E, C, 1);
        assertEquals("Error with contains no label", true, g.contains(C, A));
        assertEquals("Error with contains label", true, g.contains(B, D, 4));
        assertEquals("Error with contains", false, g.contains(D, A));
    }

    @Test
    public void testRemoveVertexDirected() {
        DirectedGraph<String, Integer> g = new DirectedGraph<String, Integer>();
        DirectedGraph<String, Integer>.Vertex A = g.add("A");
        DirectedGraph<String, Integer>.Vertex B = g.add("B");
        DirectedGraph<String, Integer>.Vertex C = g.add("C");
        DirectedGraph<String, Integer>.Vertex D = g.add("D");
        DirectedGraph<String, Integer>.Edge e1 = g.add(C, A, 2);
        DirectedGraph<String, Integer>.Edge e2 = g.add(B, D, 3);
        DirectedGraph<String, Integer>.Edge e3 = g.add(A, D, 4);
        DirectedGraph<String, Integer>.Edge e4 = g.add(D, A, 5);
        DirectedGraph<String, Integer>.Edge e5 = g.add(D, C, 6);
        g.remove(A);
        assertEquals("Error with remove1", false, g.contains(C, A));
        assertEquals("Error with remove2", false, g.contains(C, A, 2));
        assertEquals("Error with remove3", false, g.contains(A, D));
        assertEquals("Error with remove4", 3, g.vertexSize());
        assertEquals("Error with remove5", 2, g.edgeSize());
        assertEquals("Error with remove6", 1, g.inDegree(D));
        assertEquals("Error with remove7", true, g.contains(B,  D));
    }

    @Test
    public void testRemoveVertexUndirected() {
        Graph<String, Integer> g = new UndirectedGraph<String, Integer>();
        UndirectedGraph<String, Integer>.Vertex A = g.add("A");
        UndirectedGraph<String, Integer>.Vertex B = g.add("B");
        UndirectedGraph<String, Integer>.Vertex C = g.add("C");
        UndirectedGraph<String, Integer>.Vertex D = g.add("D");
        UndirectedGraph<String, Integer>.Vertex E = g.add("E");
        UndirectedGraph<String, Integer>.Edge e1 = g.add(C, A, 5);
        UndirectedGraph<String, Integer>.Edge e2 = g.add(B, D, 6);
        UndirectedGraph<String, Integer>.Edge e3 = g.add(A, D, 1);
        UndirectedGraph<String, Integer>.Edge e5 = g.add(E, C, 3);
        UndirectedGraph<String, Integer>.Edge e4 = g.add(E, B, 9);
        g.remove(A);
        assertEquals("Error with remove1", false, g.contains(A, C));
        assertEquals("Error with remove2", false, g.contains(A, D));
        assertEquals("Error with remove3", 4, g.vertexSize());
        assertEquals("Error with remove4", 3, g.edgeSize());
        assertEquals("Error with remove5", 1, g.degree(D));
        assertEquals("Error with remove6", 2, g.degree(E));
        assertEquals("Error with remove7", false, g.contains(C, A));
        assertEquals("Error with remove8", true, g.contains(E, B));
    }

    @Test
    public void testRemoveEdgeDirected() {
        DirectedGraph<String, Integer> g = new DirectedGraph<String, Integer>();
        DirectedGraph<String, Integer>.Vertex A = g.add("A");
        DirectedGraph<String, Integer>.Vertex B = g.add("B");
        DirectedGraph<String, Integer>.Vertex C = g.add("C");
        DirectedGraph<String, Integer>.Vertex D = g.add("D");
        DirectedGraph<String, Integer>.Edge e1 = g.add(C, A, 6);
        DirectedGraph<String, Integer>.Edge e2 = g.add(B, D, 1);
        DirectedGraph<String, Integer>.Edge e3 = g.add(A, D, 2);
        DirectedGraph<String, Integer>.Edge e4 = g.add(B, A, 8);
        DirectedGraph<String, Integer>.Edge e5 = g.add(D, C, 4);
        g.remove(e5);
        assertEquals("Error with remove1", false, g.contains(D, C));
        assertEquals("Error with remove2", false, g.contains(D, C, 4));
        assertEquals("Error with remove3", true, g.contains(B, A));
        assertEquals("Error with remove4", 4, g.vertexSize());
        assertEquals("Error with remove5", 4, g.edgeSize());
        assertEquals("Error with remove6", 0, g.inDegree(C));
        assertEquals("Error with remove7", 1, g.outDegree(C));
        assertEquals("Error with remove8", 0, g.outDegree(D));
        assertEquals("Error with remove9", 2, g.inDegree(D));
    }
    @Test
    public void testRemoveVerticesDirected() {
        DirectedGraph<String, Integer> g = new DirectedGraph<String, Integer>();
        DirectedGraph<String, Integer>.Vertex A = g.add("A");
        DirectedGraph<String, Integer>.Vertex B = g.add("B");
        DirectedGraph<String, Integer>.Vertex C = g.add("C");
        DirectedGraph<String, Integer>.Vertex D = g.add("D");
        DirectedGraph<String, Integer>.Vertex E = g.add("E");
        DirectedGraph<String, Integer>.Edge e1 = g.add(C, A, 1);
        DirectedGraph<String, Integer>.Edge e2 = g.add(A, D, 2);
        DirectedGraph<String, Integer>.Edge e3 = g.add(D, A, 3);
        DirectedGraph<String, Integer>.Edge e4 = g.add(E, A, 4);
        DirectedGraph<String, Integer>.Edge e5 = g.add(E, C, 5);
        g.remove(D, A);
        assertEquals("Error with remove1", true, g.contains(E, C));
        assertEquals("Error with remove2", false, g.contains(D, A));
        assertEquals("Error with remove3", 5, g.vertexSize());
        assertEquals("Error with remove4", 4, g.edgeSize());
        assertEquals("Error with remove5", 2, g.inDegree(A));
        assertEquals("Error with remove7", 1, g.inDegree(C));
        assertEquals("Error with remove6", 2, g.outDegree(E));
        assertEquals("Error with remove8", 1, g.outDegree(C));
    }

    @Test
    public void testRemoveEdgeUndirected() {
        Graph<String, Integer> g = new UndirectedGraph<String, Integer>();
        UndirectedGraph<String, Integer>.Vertex A = g.add("A");
        UndirectedGraph<String, Integer>.Vertex B = g.add("B");
        UndirectedGraph<String, Integer>.Vertex C = g.add("C");
        UndirectedGraph<String, Integer>.Vertex D = g.add("D");
        UndirectedGraph<String, Integer>.Vertex E = g.add("E");
        UndirectedGraph<String, Integer>.Edge e1 = g.add(C, A, 1);
        UndirectedGraph<String, Integer>.Edge e2 = g.add(B, D, 2);
        UndirectedGraph<String, Integer>.Edge e3 = g.add(A, D, 3);
        UndirectedGraph<String, Integer>.Edge e4 = g.add(E, A, 4);
        g.remove(e1);
        assertEquals("Error with remove1", false, g.contains(A, C));
        assertEquals("Error with remove2", false, g.contains(A, C, 1));
        assertEquals("Error with remove3", 5, g.vertexSize());
        assertEquals("Error with remove4", 3, g.edgeSize());
        assertEquals("Error with remove5", 0, g.degree(C));
        assertEquals("Error with remove6", 2, g.degree(A));
        assertEquals("Error with remove7", false, g.contains(C, A));
    }
    @Test
    public void testRemoveVerticesUndirected() {
        Graph<String, Integer> g = new UndirectedGraph<String, Integer>();
        UndirectedGraph<String, Integer>.Vertex A = g.add("A");
        UndirectedGraph<String, Integer>.Vertex B = g.add("B");
        UndirectedGraph<String, Integer>.Vertex C = g.add("C");
        UndirectedGraph<String, Integer>.Vertex D = g.add("D");
        UndirectedGraph<String, Integer>.Vertex E = g.add("E");
        UndirectedGraph<String, Integer>.Edge e1 = g.add(C, A, 5);
        UndirectedGraph<String, Integer>.Edge e2 = g.add(B, D, 4);
        UndirectedGraph<String, Integer>.Edge e3 = g.add(A, D, 3);
        UndirectedGraph<String, Integer>.Edge e4 = g.add(E, A, 2);
        UndirectedGraph<String, Integer>.Edge e5 = g.add(E, C, 1);
        g.remove(A, E);
        assertEquals("Error with remove1", false, g.contains(A, E));
        assertEquals("Error with remove2", false, g.contains(E, A, 2));
        assertEquals("Error with remove3", 5, g.vertexSize());
        assertEquals("Error with remove4", 4, g.edgeSize());
        assertEquals("Error with remove5", 2, g.degree(C));
        assertEquals("Error with remove6", 2, g.degree(A));
        assertEquals("Error with remove7", 1, g.degree(E));
        assertEquals("Error with remove8", false, g.contains(E, A));
    }
    @Test
    public void test1() {
        Graph<Integer, Integer> g = new UndirectedGraph<Integer, Integer>();
        UndirectedGraph<Integer, Integer>.Vertex v0 = g.add(0);
        UndirectedGraph<Integer, Integer>.Vertex v1 = g.add(1);
        UndirectedGraph<Integer, Integer>.Vertex v2 = g.add(2);
        UndirectedGraph<Integer, Integer>.Vertex v3 = g.add(3);
        UndirectedGraph<Integer, Integer>.Vertex v4 = g.add(4);
        UndirectedGraph<Integer, Integer>.Vertex v5 = g.add(5);
        UndirectedGraph<Integer, Integer>.Vertex v6 = g.add(6);
        UndirectedGraph<Integer, Integer>.Vertex v7 = g.add(7);
        UndirectedGraph<Integer, Integer>.Vertex v8 = g.add(8);
        UndirectedGraph<Integer, Integer>.Vertex v9 = g.add(9);
        UndirectedGraph<Integer, Integer>.Edge e0 = g.add(v0, v1, 0);
        UndirectedGraph<Integer, Integer>.Edge e1 = g.add(v0, v2, 1);
        UndirectedGraph<Integer, Integer>.Edge e2 = g.add(v0, v3, 2);
        UndirectedGraph<Integer, Integer>.Edge e3 = g.add(v1, v2, 3);
        UndirectedGraph<Integer, Integer>.Edge e4 = g.add(v1, v4, 4);
        UndirectedGraph<Integer, Integer>.Edge e5 = g.add(v1, v5, 5);
        UndirectedGraph<Integer, Integer>.Edge e6 = g.add(v2, v6, 6);
        UndirectedGraph<Integer, Integer>.Edge e7 = g.add(v2, v7, 7);
        UndirectedGraph<Integer, Integer>.Edge e8 = g.add(v7, v0, 8);
        UndirectedGraph<Integer, Integer>.Edge e9 = g.add(v7, v8, 9);
        UndirectedGraph<Integer, Integer>.Edge e10 = g.add(v7, v9, 10);
        UndirectedGraph<Integer, Integer>.Edge e11 = g.add(v9, v7, 11);
        g.remove(v2);
        assertEquals("Error1: ", 8, g.edgeSize());
    }
}

