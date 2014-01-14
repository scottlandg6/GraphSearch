package graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/** Assorted graph algorithms.
 *  @author Scott Lee
 */
public final class Graphs {

    /* A* Search Algorithms */

    /** Returns a path from V0 to V1 in G of minimum weight, according
     *  to the edge weighter EWEIGHTER.  VLABEL and ELABEL are the types of
     *  vertex and edge labels.  Assumes that H is a distance measure
     *  between vertices satisfying the two properties:
     *     a. H.dist(v, V1) <= shortest path from v to V1 for any v, and
     *     b. H.dist(v, w) <= H.dist(w, V1) + weight of edge (v, w), where
     *        v and w are any vertices in G.
     *
     *  As a side effect, uses VWEIGHTER to set the weight of vertex v
     *  to the weight of a minimal path from V0 to v, for each v in
     *  the returned path and for each v such that
     *       minimum path length from V0 to v + H.dist(v, V1)
     *              < minimum path length from V0 to V1.
     *  The final weights of other vertices are not defined.  If V1 is
     *  unreachable from V0, returns null and sets the minimum path weights of
     *  all reachable nodes.  The distance to a node unreachable from V0 is
     *  Double.POSITIVE_INFINITY. */
    public static <VLabel, ELabel> List<Graph<VLabel, ELabel>.Edge>
    shortestPath(Graph<VLabel, ELabel> G,
                 Graph<VLabel, ELabel>.Vertex V0,
                 Graph<VLabel, ELabel>.Vertex V1,
                 Distancer<? super VLabel> h,
                 Weighter<? super VLabel> vweighter,
                 Weighting<? super ELabel> eweighter) {
        PriorityQueue<Calcvert<VLabel, ELabel>> queue = new
                PriorityQueue<Calcvert<VLabel, ELabel>>();
        HashMap<Graph<VLabel, ELabel>.Vertex, Calcvert<VLabel,
        ELabel>> closedmap = new HashMap<Graph<VLabel, ELabel>.Vertex,
                Calcvert<VLabel, ELabel>>();
        HashMap<Graph<VLabel, ELabel>.Vertex,
        Calcvert<VLabel, ELabel>> openmap =
                new HashMap<Graph<VLabel, ELabel>.Vertex,
                Calcvert<VLabel, ELabel>>();
        double gscore = 0.0;
        vweighter.setWeight(V0.getLabel(), gscore);
        double fscore = gscore + h.dist(V0.getLabel(), V1.getLabel());
        queue.add(new Calcvert<VLabel, ELabel>(V0, fscore, gscore));
        openmap.put(V0, queue.peek());

        while (!queue.isEmpty()) {
            Calcvert<VLabel, ELabel> current = queue.poll();
            openmap.remove(current.getVertex());
            if (current.getVertex().equals(V1)) {
                return constructor(current);
            }
            closedmap.put(current.getVertex(), current);

            Iteration<Graph<VLabel, ELabel>.Edge> neighborEdges =
                    G.outEdges(current.getVertex());
            for (Graph<VLabel, ELabel>.Edge e : neighborEdges) {
                Graph<VLabel, ELabel>.Vertex neighborVertex =
                        e.getV(current.getVertex());
                double tentativeg = current.getG()
                        + eweighter.weight(e.getLabel());
                double tentativef = tentativeg
                        + h.dist(neighborVertex.getLabel(), V1.getLabel());
                if (closedmap.containsKey(neighborVertex)
                        && tentativef >= closedmap.get(
                                neighborVertex).getF()) {
                    continue;
                }
                if (!openmap.containsKey(neighborVertex)
                        || tentativef < openmap.get(neighborVertex).getF()) {
                    Calcvert<VLabel, ELabel> neighbor = new
                            Calcvert<VLabel, ELabel>(
                                    neighborVertex, tentativef, tentativeg);
                    vweighter.setWeight(neighborVertex.getLabel(), tentativeg);
                    neighbor.setParent(e, current);
                    if (openmap.containsKey(neighborVertex)) {
                        queue.remove(openmap.get(neighborVertex));
                    }
                    queue.add(neighbor);
                    openmap.put(neighborVertex, neighbor);
                }
            }
        }
        vweighter.setWeight(V1.getLabel(), Double.POSITIVE_INFINITY);
        return null;

    }

    /** Returns a List<Graph<VLabel, ELabel>.Edge, of the edge path
     *  from the beginning of the path to END, with VLABEL and ELABEL. */
    private static <VLabel, ELabel> List<Graph<VLabel, ELabel>.Edge>
    constructor(Calcvert<VLabel, ELabel> end) {
        LinkedList<Graph<VLabel, ELabel>.Edge> answer =
                new LinkedList<Graph<VLabel, ELabel>.Edge>();
        Calcvert<VLabel, ELabel> curr = end;
        while (curr.getParentEdge() != null) {
            answer.addFirst(curr.getParentEdge());
            curr = curr.getParentVert();
        }
        return (List<Graph<VLabel, ELabel>.Edge>) answer;
    }

    /** Returns a path from V0 to V1 in G of minimum weight, according
     *  to the weights of its edge labels.  VLABEL and ELABEL are the types of
     *  vertex and edge labels.  Assumes that H is a distance measure
     *  between vertices satisfying the two properties:
     *     a. H.dist(v, V1) <= shortest path from v to V1 for any v, and
     *     b. H.dist(v, w) <= H.dist(w, V1) + weight of edge (v, w), where
     *        v and w are any vertices in G.
     *
     *  As a side effect, sets the weight of vertex v to the weight of
     *  a minimal path from V0 to v, for each v in the returned path
     *  and for each v such that
     *       minimum path length from V0 to v + H.dist(v, V1)
     *           < minimum path length from V0 to V1.
     *  The final weights of other vertices are not defined.
     *
     *  This function has the same effect as the 6-argument version of
     *  shortestPath, but uses the .weight and .setWeight methods of
     *  the edges and vertices themselves to determine and set
     *  weights. If V1 is unreachable from V0, returns null and sets
     *  the minimum path weights of all reachable nodes.  The distance
     *  to a node unreachable from V0 is Double.POSITIVE_INFINITY. */
    public static
    <VLabel extends Weightable, ELabel extends Weighted>
    List<Graph<VLabel, ELabel>.Edge>
    shortestPath(Graph<VLabel, ELabel> G,
                 Graph<VLabel, ELabel>.Vertex V0,
                 Graph<VLabel, ELabel>.Vertex V1,
                 Distancer<? super VLabel> h) {
        PriorityQueue<Calcvert<VLabel, ELabel>> queue = new
                PriorityQueue<Calcvert<VLabel, ELabel>>();
        HashMap<Graph<VLabel, ELabel>.Vertex, Calcvert<VLabel, ELabel>>
        closedmap = new HashMap<Graph<VLabel, ELabel>.Vertex,
                Calcvert<VLabel, ELabel>>();
        HashMap<Graph<VLabel, ELabel>.Vertex,
        Calcvert<VLabel, ELabel>> openmap =
            new HashMap<Graph<VLabel, ELabel>.Vertex,
                Calcvert<VLabel, ELabel>>();
        double g = 0.0;
        V0.getLabel().setWeight(g);
        double f = g + h.dist(V0.getLabel(), V1.getLabel());
        queue.add(new Calcvert<VLabel, ELabel>(V0, f, g));
        openmap.put(V0, queue.peek());

        while (!queue.isEmpty()) {
            Calcvert<VLabel, ELabel> current = queue.poll();
            openmap.remove(current.getVertex());
            if (current.getVertex().equals(V1)) {
                return constructor(current);
            }
            closedmap.put(current.getVertex(), current);

            Iteration<Graph<VLabel, ELabel>.Edge> neighborEdges =
                    G.outEdges(current.getVertex());
            for (Graph<VLabel, ELabel>.Edge e : neighborEdges) {
                Graph<VLabel, ELabel>.Vertex neighborVertices =
                        e.getV(current.getVertex());
                double tentativeg = current.getG() + e.getLabel().weight();
                double tentativef = tentativeg + h.dist(
                        neighborVertices.getLabel(), V1.getLabel());
                if (closedmap.containsKey(neighborVertices)
                        && tentativef >= closedmap.get(
                                neighborVertices).getF()) {
                    continue;
                }
                if (!openmap.containsKey(neighborVertices)
                        || tentativef < openmap.get(
                                neighborVertices).getF()) {
                    Calcvert<VLabel, ELabel> neighbor = new
                            Calcvert<VLabel, ELabel>(
                                    neighborVertices, tentativef, tentativeg);
                    neighborVertices.getLabel().setWeight(tentativeg);
                    neighbor.setParent(e, current);
                    if (openmap.containsKey(neighborVertices)) {
                        queue.remove(openmap.get(neighborVertices));
                    }
                    queue.add(neighbor);
                    openmap.put(neighborVertices, neighbor);
                }
            }
        }
        V1.getLabel().setWeight(Double.POSITIVE_INFINITY);
        return null;
    }

    /** Returns a distancer whose dist method always returns 0. */
    public static final Distancer<Object> ZERO_DISTANCER =
        new Distancer<Object>() {
            @Override
            public double dist(Object v0, Object v1) {
                return 0.0;
            }
        };
    /** Represents vertices with their calculated f and g scores. */
    private static class Calcvert<VLabel, ELabel> implements
        Comparable<Calcvert<VLabel, ELabel>> {

        /** A new representation of the vertex VERT with FSCORE and GSCORE. */
        Calcvert(Graph<VLabel, ELabel>.Vertex vert, double fscore,
                double gscore) {
            _vertex = vert;
            _fscore = fscore;
            _gscore = gscore;
            _parentEdge = null;
            _parentVertex = null;
        }
        @Override
        public int compareTo(Calcvert<VLabel, ELabel> other) {
            return Double.compare(this.getF(), other.getF());
        }
        /** Returns _vertex. */
        Graph<VLabel, ELabel>.Vertex getVertex() {
            return _vertex;
        }
        /** Returns _gscore. */
        double getG() {
            return _gscore;
        }
        /** Returns _fscore. */
        double getF() {
            return _fscore;
        }
        /** Sets _parentEdge to E and _parentVert to V. */
        void setParent(Graph<VLabel, ELabel>.Edge e,
                Calcvert<VLabel, ELabel> v) {
            _parentEdge = e;
            _parentVertex = v;
        }
        /** Returns _parentEdge. */
        Graph<VLabel, ELabel>.Edge getParentEdge() {
            return _parentEdge;
        }
        /** Returns _parentVert. */
        Calcvert<VLabel, ELabel> getParentVert() {
            return _parentVertex;
        }
        /** THIS vertex. */
        private Graph<VLabel, ELabel>.Vertex _vertex;
        /** The f value for THIS vertex. */
        private double _fscore;
        /** The g value for THIS vertex. */
        private double _gscore;
        /** The parent edge to THIS vertex. */
        private Graph<VLabel, ELabel>.Edge _parentEdge;
        /** The parent vertex to THIS vertex. */
        private Calcvert<VLabel, ELabel> _parentVertex;
    }
}
