package graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/* Do not add or remove public or protected members, or modify the signatures of
 * any public methods.  You may make changes that don't affect the API as seen
 * from outside the graph package:
 *   + You may make methods in Graph abstract, if you want different
 *     implementations in DirectedGraph and UndirectedGraph.
 *   + You may add bodies to abstract methods, modify existing bodies,
 *     or override inherited methods.
 *   + You may change parameter names, or add 'final' modifiers to parameters.
 *   + You may private and package private members.
 *   + You may add additional non-public classes to the graph package.
 */

/** Represents a general graph whose vertices are labeled with a type
 *  VLABEL and whose edges are labeled with a type ELABEL. The
 *  vertices are represented by the inner type Vertex and edges by
 *  inner type Edge.  A graph may be directed or undirected.  For
 *  an undirected graph, outgoing and incoming edges are the same.
 *  Graphs may have self edges and may have multiple edges between vertices.
 *
 *  The vertices and edges of the graph, the edges incident on a
 *  vertex, and the neighbors of a vertex are all accessible by
 *  iterators.  Changing the graph's structure by adding or deleting
 *  edges or vertices invalidates these iterators (subsequent use of
 *  them is undefined.)
 *  @author Scott Lee
 */
public abstract class Graph<VLabel, ELabel> {

    /** Represents one of my vertices. */
    public class Vertex {

        /** A new vertex with LABEL as the value of getLabel(). */
        Vertex(VLabel label) {
            _label = label;
        }

        /** Returns the label on this vertex. */
        public VLabel getLabel() {
            return _label;
        }

        @Override
        public String toString() {
            return String.valueOf(_label);
        }

        /** The label on this vertex. */
        private final VLabel _label;

    }

    /** Represents one of my edges. */
    public class Edge {

        /** An edge (V0,V1) with label LABEL.  It is a directed edge (from
         *  V0 to V1) in a directed graph. */
        Edge(Vertex v0, Vertex v1, ELabel label) {
            _label = label;
            _v0 = v0;
            _v1 = v1;
        }

        /** Returns the label on this edge. */
        public ELabel getLabel() {
            return _label;
        }

        /** Return the vertex this edge exits. For an undirected edge, this is
         *  one of the incident vertices. */
        public Vertex getV0() {
            return _v0;
        }

        /** Return the vertex this edge enters. For an undirected edge, this is
         *  the incident vertices other than getV1(). */
        public Vertex getV1() {
            return _v1;
        }

        /** Returns the vertex at the other end of me from V.  */
        public final Vertex getV(Vertex v) {
            if (v == _v0) {
                return _v1;
            } else if (v == _v1) {
                return _v0;
            } else {
                throw new
                    IllegalArgumentException("vertex not incident to edge");
            }
        }

        @Override
        public String toString() {
            return String.format("(%s,%s):%s", _v0, _v1, _label);
        }

        /** Endpoints of this edge.  In directed edges, this edge exits _V0
         *  and enters _V1. */
        private final Vertex _v0, _v1;

        /** The label on this edge. */
        private final ELabel _label;

    }

    /*=====  Methods and variables of Graph =====*/

    /** Returns the number of vertices in me. */
    public int vertexSize() {
        return vertices.size();
    }

    /** Returns the number of edges in me. */
    public int edgeSize() {
        return edges.size();
    }

    /** Returns true iff I am a directed graph. */
    public abstract boolean isDirected();

    /** Returns the number of outgoing edges incident to V. Assumes V is one of
     *  my vertices.  */
    public int outDegree(Vertex v) {
        int a = 0;
        if (isDirected()) {
            for (int x = 0; x < edgemap.get(v).size(); x++) {
                if (edgemap.get(v).get(x).getV0() == v) {
                    a++;
                }
            }
        } else {
            for (int x = 0; x < edges.size(); x++) {
                if (edges.get(x).getV0() == v || edges.get(x).getV1() == v) {
                    a++;
                }
            }
        }
        return a;
    }

    /** Returns the number of incoming edges incident to V. Assumes V is one of
     *  my vertices. */
    public int inDegree(Vertex v) {
        int a = 0;
        for (int x = 0; x < edges.size(); x++) {
            if (edges.get(x).getV1() == v) {
                a++;
            }
        }
        return a;
    }

    /** Returns outDegree(V). This is simply a synonym, intended for
     *  use in undirected graphs. */
    public final int degree(Vertex v) {
        return outDegree(v);
    }

    /** Returns true iff there is an edge (U, V) in me with any label. */
    public boolean contains(Vertex u, Vertex v) {
        boolean a = false;
        for (int x = 0; x < edgeSize(); x++) {
            if (edges.get(x).getV0() == u) {
                if (edges.get(x).getV1() == v) {
                    a = true;
                }
            }
        }
        if (!isDirected()) {
            for (int x = 0; x < edgeSize(); x++) {
                if (edges.get(x).getV1() == u) {
                    if (edges.get(x).getV0() == v) {
                        a = true;
                    }
                }
            }
        }
        return a;
    }

    /** Returns true iff there is an edge (U, V) in me with label LABEL. */
    public boolean contains(Vertex u, Vertex v,
                            ELabel label) {
        boolean a = false;
        for (int x = 0; x < edgeSize(); x++) {
            if (edges.get(x).getV0() == u) {
                if (edges.get(x).getV1() == v) {
                    if (edges.get(x).getLabel() == label) {
                        a = true;
                    }
                }
            }
        }
        if (!isDirected()) {
            for (int x = 0; x < edgeSize(); x++) {
                if (edges.get(x).getV1() == u) {
                    if (edges.get(x).getV0() == v) {
                        if (edges.get(x).getLabel() == label) {
                            a = true;
                        }
                    }
                }
            }
        }
        return a;
    }

    /** Returns a new vertex labeled LABEL, and adds it to me with no
     *  incident edges. */
    public Vertex add(VLabel label) {
        Vertex a = new Vertex(label);
        vertices.add(a);
        ArrayList<Edge> edgers = new ArrayList<Edge>();
        edgemap.put(a, edgers);
        return a;
    }

    /** Returns an edge incident on FROM and TO, labeled with LABEL
     *  and adds it to this graph. If I am directed, the edge is directed
     *  (leaves FROM and enters TO). */
    public Edge add(Vertex from,
                    Vertex to,
                    ELabel label) {
        Edge b = new Edge(from, to, label);
        edgemap.get(from).add(b);
        edgemap.get(to).add(b);
        edges.add(b);
        return b;
    }

    /** Returns an edge incident on FROM and TO with a null label
     *  and adds it to this graph. If I am directed, the edge is directed
     *  (leaves FROM and enters TO). */
    public Edge add(Vertex from,
                    Vertex to) {
        Edge b = new Edge(from, to, null);
        edgemap.get(from).add(b);
        edgemap.get(to).add(b);
        edges.add(b);
        return b;
    }

    /** Remove V and all adjacent edges, if present. */
    public void remove(Vertex v) {
        for (int y = 0; y < vertices.size(); y++) {
            for (int z = 0; z < edgemap.get(vertices.get(y)).size();) {
                if (edgemap.get(vertices.get(y)).get(z).getV0() == v
                        || edgemap.get(vertices.get(y)).get(z).getV1() == v) {
                    edgemap.get(vertices.get(y)).remove(z);
                } else {
                    z++;
                }
            }
        }
        for (int x = 0; x < edges.size();) {
            if (edges.get(x).getV0() == v) {
                edges.remove(x);
            } else if (edges.get(x).getV1() == v) {
                edges.remove(x);
            } else {
                x++;
            }
        }
        vertices.remove(v);
        edgemap.remove(v);
    }

    /** Remove E from me, if present.  E must be between my vertices,
     *  or the result is undefined.  */
    public void remove(Edge e) {
        edges.remove(e);
        for (int x = 0; x < vertices.size(); x++) {
            for (int y = 0; y < edgemap.get(vertices.get(x)).size();) {
                if (edgemap.get(vertices.get(x)).get(y).getLabel()
                        == e.getLabel()) {
                    edgemap.get(vertices.get(x)).remove(y);
                } else {
                    y++;
                }
            }
        }
    }

    /** Remove all edges from V1 to V2 from me, if present.  The result is
     *  undefined if V1 and V2 are not among my vertices.  */
    public void remove(Vertex v1, Vertex v2) {
        for (int x = 0; x < edges.size();) {
            if (edges.get(x).getV0() == v1 && edges.get(x).getV1() == v2) {
                edges.remove(x);
            } else {
                x++;
            }
        }
        for (int x = 0; x < edgemap.get(v1).size(); x++) {
            if (edgemap.get(v1).get(x).getV1() == v2) {
                edgemap.get(v1).remove(x);
            }
        }
        for (int x = 0; x < edgemap.get(v2).size(); x++) {
            if (edgemap.get(v2).get(x).getV0() == v1) {
                edgemap.get(v2).remove(x);
            }
        }
        if (!isDirected()) {
            for (int x = 0; x < edges.size();) {
                if (edges.get(x).getV1() == v1 && edges.get(x).getV0() == v2) {
                    edges.remove(x);
                } else {
                    x++;
                }
            }
            for (int x = 0; x < edgemap.get(v1).size(); x++) {
                if (edgemap.get(v1).get(x).getV1() == v1) {
                    edgemap.get(v1).remove(x);
                }
            }
            for (int x = 0; x < edgemap.get(v2).size(); x++) {
                if (edgemap.get(v2).get(x).getV0() == v2) {
                    edgemap.get(v2).remove(x);
                }
            }
        }
    }

    /** Returns an Iterator over all vertices in arbitrary order. */
    public Iteration<Vertex> vertices() {
        Iterator<Vertex> vertexes = vertices.iterator();
        return Iteration.iteration(vertexes);
    }

    /** Returns an iterator over all successors of V. */
    public Iteration<Vertex> successors(Vertex v) {
        ArrayList<Vertex> success = new ArrayList<Vertex>();
        for (int x = 0; x < edgemap.get(v).size(); x++) {
            if (edgemap.get(v).get(x).getV0() == v) {
                success.add(edgemap.get(v).get(x).getV1());
            }
            if (!isDirected()) {
                if (edgemap.get(v).get(x).getV1() == v) {
                    success.add(edgemap.get(v).get(x).getV0());
                }
            }
        }
        Iterator<Vertex> successor = success.iterator();
        return Iteration.iteration(successor);
    }

    /** Returns an iterator over all predecessors of V. */
    public Iteration<Vertex> predecessors(Vertex v) {
        ArrayList<Vertex> predecess = new ArrayList<Vertex>();
        for (int x = 0; x < edgemap.get(v).size(); x++) {
            if (edgemap.get(v).get(x).getV1() == v) {
                predecess.add(edgemap.get(v).get(x).getV0());
            }
        }
        Iterator<Vertex> predecessor = predecess.iterator();
        return Iteration.iteration(predecessor);
    }

    /** Returns successors(V).  This is a synonym typically used on
     *  undirected graphs. */
    public final Iteration<Vertex> neighbors(Vertex v) {
        return successors(v);
    }

    /** Returns an iterator over all edges in me. */
    public Iteration<Edge> edges() {
        Iterator<Edge> edge = edges.iterator();
        return Iteration.iteration(edge);
    }

    /** Returns iterator over all outgoing edges from V. */
    public Iteration<Edge> outEdges(Vertex v) {
        if (!isDirected()) {
            Iterator<Edge> outedges = edgemap.get(v).iterator();
            return Iteration.iteration(outedges);
        }
        ArrayList<Edge> outedge = new ArrayList<Edge>();
        for (int x = 0; x < edgemap.get(v).size(); x++) {
            if (edgemap.get(v).get(x).getV0() == v) {
                outedge.add(edgemap.get(v).get(x));
            }
        }
        Iterator<Edge> outedges = outedge.iterator();
        return Iteration.iteration(outedges);
    }

    /** Returns iterator over all incoming edges to V. */
    public Iteration<Edge> inEdges(Vertex v) {
        ArrayList<Edge> inedge = new ArrayList<Edge>();
        for (int x = 0; x < edgemap.get(v).size(); x++) {
            if (edgemap.get(v).get(x).getV1() == v) {
                inedge.add(edgemap.get(v).get(x));
            }
        }
        Iterator<Edge> inedges = inedge.iterator();
        return Iteration.iteration(inedges);
    }

    /** Returns outEdges(V). This is a synonym typically used
     *  on undirected graphs. */
    public final Iteration<Edge> edges(Vertex v) {
        return outEdges(v);
    }

    /** Returns the natural ordering on T, as a Comparator.  For
     *  example, if stringComp = Graph.<Integer>naturalOrder(), then
     *  stringComp.compare(x1, y1) is <0 if x1<y1, ==0 if x1=y1, and >0
     *  otherwise. */
    public static <T extends Comparable<? super T>> Comparator<T> naturalOrder()
    {
        return new Comparator<T>() {
            @Override
            public int compare(T x1, T x2) {
                return x1.compareTo(x2);
            }
        };
    }

    /** Cause subsequent calls to edges() to visit or deliver
     *  edges in sorted order, according to COMPARATOR. Subsequent
     *  addition of edges may cause the edges to be reordered
     *  arbitrarily.  */
    public void orderEdges(Comparator<ELabel> comparator) {
    }
    /** Arraylist to hold vertices. */
    private ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    /** HashMap to hold edges. */
    private HashMap<Vertex, ArrayList<Edge>> edgemap =
            new HashMap<Vertex, ArrayList<Edge>>();
    /** Arraylist to hold edges. */
    private ArrayList<Edge> edges = new ArrayList<Edge>();

}
