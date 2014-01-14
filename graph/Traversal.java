package graph;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.Stack;
import java.util.LinkedList;
import java.util.PriorityQueue;

/** Implements a generalized traversal of a graph.  At any given time,
 *  there is a particular set of untraversed vertices---the "fringe."
 *  Traversal consists of repeatedly removing an untraversed vertex
 *  from the fringe, visting it, and then adding its untraversed
 *  successors to the fringe.  The client can dictate an ordering on
 *  the fringe, determining which item is next removed, by which kind
 *  of traversal is requested.
 *     + A depth-first traversal treats the fringe as a list, and adds
 *       and removes vertices at one end.  It also revisits the node
 *       itself after traversing all successors by calling the
 *       postVisit method on it.
 *     + A breadth-first traversal treats the fringe as a list, and adds
 *       and removes vertices at different ends.  It also revisits the node
 *       itself after traversing all successors as for depth-first
 *       traversals.
 *     + A general traversal treats the fringe as an ordered set, as
 *       determined by a Comparator argument.  There is no postVisit
 *       for this type of traversal.
 *  As vertices are added to the fringe, the traversal calls a
 *  preVisit method on the vertex.
 *
 *  Generally, the client will extend Traversal, overriding the visit,
 *  preVisit, and postVisit methods, as desired (by default, they do nothing).
 *  Any of these methods may throw StopException to halt the traversal
 *  (temporarily, if desired).  The preVisit method may throw a
 *  RejectException to prevent a vertex from being added to the
 *  fringe, and the visit method may throw a RejectException to
 *  prevent its successors from being added to the fringe.
 *  @author Scott Lee
 */
public class Traversal<VLabel, ELabel> {

    /** Perform a traversal of G over all vertices reachable from V.
     *  ORDER determines the ordering in which the fringe of
     *  untraversed vertices is visited. The effect of specifying an
     *  ORDER whose results change as a result of modifications made during the
     *  traversal is undefined.*/
    public void traverse(Graph<VLabel, ELabel> G,
                         Graph<VLabel, ELabel>.Vertex v,
                         Comparator<VLabel> order) {
        if (!_traversalsearch) {
            _visited.clear();
        }
        if (_visited.contains(v)) {
            _traversalsearch = false;
            return;
        }
        boolean stop = false;
        PriorityQueue<Graph<VLabel, ELabel>.Vertex> queue = new
                PriorityQueue<Graph<VLabel, ELabel>.Vertex>(
                _graph.vertexSize(), new Comparer(order));
        _graph = G;
        _comparer = order;
        _traversalsearch = true;
        queue.add(v);
        while (!queue.isEmpty()) {
            _visited.add(queue.peek());
            try {
                visit(queue.peek());
            } catch (StopException traversalStop) {
                _finalEdge = null;
                _finalVertex = queue.peek();
                return;
            } catch (RejectException traversalReject) {
                stop = true;
            }
            if (!stop) {
                Graph<VLabel, ELabel>.Vertex current = queue.poll();
                for (Graph<VLabel, ELabel>.Edge edge : G.outEdges(current)) {
                    if (!_visited.contains(edge.getV(current))) {
                        try {
                            preVisit(edge, edge.getV(current));
                            queue.add(edge.getV(current));
                        } catch (StopException terminate) {
                            return;
                        } catch (RejectException terminate) {
                            return;
                        }
                    }
                }
            } else {
                queue.poll();
            }
        }
        _traversalsearch = false;
    }

    /** Helper for depthFirstTraverse that iterates over the graph GRAPH with
     *  the post visit stack STACK. */
    public void depthFirstHelper(Graph<VLabel, ELabel> graph,
            Stack<Graph<VLabel, ELabel>.Vertex> stack) {
        boolean stop = false;
        while (!stack.empty()) {
            if (!_visited.contains(stack.peek())) {
                _visited.add(stack.peek());
                try {
                    visit(stack.peek());
                } catch (StopException depthStop) {
                    _finalEdge = null;
                    _finalVertex = stack.peek();
                    return;
                } catch (RejectException depthReject) {
                    stop = true;
                }
                if (!stop) {
                    Graph<VLabel, ELabel>.Vertex current = stack.peek();
                    for (Graph<VLabel, ELabel>.Edge edge
                            : graph.outEdges(current)) {
                        if (!_visited.contains(edge.getV(current))) {
                            try {
                                preVisit(edge, current);
                                stack.addElement(edge.getV(current));
                            } catch (StopException depthStop) {
                                _finalEdge = edge;
                                _finalVertex =
                                        edge.getV(edge.getV(current));
                                return;
                            } catch (RejectException depthReject) {
                                continue;
                            }
                        }
                    }
                } else {
                    if (!_depthpost.contains(stack.peek())) {
                        if (depthPoster(stack) == 0) {
                            return;
                        }
                    }
                    stack.pop();
                    stop = false;
                }
            } else {
                if (!_depthpost.contains(stack.peek())) {
                    if (depthPoster(stack) != 1) {
                        return;
                    }
                }
                stack.pop();
            }
        }
        _depthsearch = false;
    }

    /** Helper for a depthFirstTraverse that performs a visit on the first
     *  element of post visit stack STACK. Returns 0 for a StopException and
     *  1 otherwise. */
    public int depthPoster(Stack<Graph<VLabel, ELabel>.Vertex> stack) {
        _depthpost.add(stack.peek());
        try {
            postVisit(stack.peek());
        } catch (StopException depthStop) {
            _finalEdge = null;
            _finalVertex = stack.pop();
            return 0;
        } catch (RejectException depthReject) {
            return 1;
        }
        return 1;
    }
    /** Performs a depth-first traversal of G over all vertices
     *  reachable from V.  That is, the fringe is a sequence and
     *  vertices are added to it or removed from it at one end in
     *  an undefined order.  After the traversal of all successors of
     *  a node is complete, the node itself is revisited by calling
     *  the postVisit method on it. */
    public void depthFirstTraverse(Graph<VLabel, ELabel> G,
                                   Graph<VLabel, ELabel>.Vertex v) {
        if (!_depthsearch) {
            _depthpost.clear();
            _visited.clear();
        }
        if (_visited.contains(v)) {
            _depthsearch = false;
            return;
        }
        Stack<Graph<VLabel, ELabel>.Vertex> stack = new
                Stack<Graph<VLabel, ELabel>.Vertex>();
        stack.addElement(v);
        _graph = G;
        _depthsearch = true;
        depthFirstHelper(G, stack);
    }

    /** A helper for breadthFirstTravere that takes in the graph GRAPH that
     *  is currently being traversed and a LinkedList FRINGER that contains
     *  vertices in queue to be visited or post visited. */
    public void breadthHelper(Graph<VLabel, ELabel> graph,
            LinkedList<Graph<VLabel, ELabel>.Vertex> fringer) {
        int index = 0;
        boolean stop = false;
        while (fringer.peekFirst() != null) {
            if (_breadthpost.contains(fringer.peek())) {
                try {
                    postVisit(fringer.peek());
                } catch (StopException breadthStop) {
                    _breadthpost.clear();
                    _finalEdge = null;
                    _finalVertex = fringer.pop();
                    return;
                } catch (RejectException breadthReject) {
                    continue;
                }
                fringer.removeFirst();
            } else {
                try {
                    _visited.add(fringer.peek());
                    visit(fringer.peek());
                } catch (StopException breadthStop) {
                    _breadthpost.clear();
                    _finalEdge = null;
                    _finalVertex = fringer.removeFirst();
                    return;
                } catch (RejectException breadthReject) {
                    stop = true;
                }
                _breadthpost.add(fringer.peek());
                if (!stop) {
                    Graph<VLabel, ELabel>.Vertex current =
                            fringer.removeFirst();
                    for (Graph<VLabel, ELabel>.Edge e
                            : graph.outEdges(current)) {
                        if (!_visited.contains(e.getV(current))) {
                            try {
                                preVisit(e, current);
                                if (!_breadthpre.contains(e.getV(current))) {
                                    fringer.add(e.getV(current));
                                    _breadthpre.add(e.getV(current));
                                }
                            } catch (StopException breadthStop) {
                                _breadthpost.clear();
                                _finalEdge = e;
                                _finalVertex = e.getV(current);
                                return;
                            } catch (RejectException breadthReject) {
                                continue;
                            }
                        }
                    }
                } else {
                    fringer.removeFirst();
                }
                fringer.add(_breadthpost.get(index));
                index++;
            }
        }
        _breadthsearch = false;
    }

    /** Performs a breadth-first traversal of G over all vertices
     *  reachable from V.  That is, the fringe is a sequence and
     *  vertices are added to it at one end and removed from it at the
     *  other in an undefined order.  After the traversal of all successors of
     *  a node is complete, the node itself is revisited by calling
     *  the postVisit method on it. */
    public void breadthFirstTraverse(Graph<VLabel, ELabel> G,
                                     Graph<VLabel, ELabel>.Vertex v) {
        if (!_breadthsearch) {
            _breadthpost.clear();
            _visited.clear();
        }
        if (_visited.contains(v)) {
            _breadthsearch = false;
            return;
        }
        _breadthsearch = true;
        _graph = G;
        LinkedList<Graph<VLabel, ELabel>.Vertex> fringer = new
                LinkedList<Graph<VLabel, ELabel>.Vertex>();
        fringer.add(v);
        breadthHelper(G, fringer);
    }

    /** Continue the previous traversal starting from V.
     *  Continuing a traversal means that we do not traverse
     *  vertices or edges that have been traversed previously. */
    public void continueTraversing(Graph<VLabel, ELabel>.Vertex v) {
        if (_traversalsearch) {
            traverse(_graph, v, _comparer);
        } else if (_depthsearch) {
            depthFirstTraverse(_graph, v);
        } else {
            breadthFirstTraverse(_graph, v);
        }
    }

    /** A comparator that compares vertices. */
    private class Comparer implements
        Comparator<Graph<VLabel, ELabel>.Vertex> {

        /** Constructor that stores a Comparator<VLabel> COMPARATOR in
         *  variable _COMPARATOR. */
        public Comparer(Comparator<VLabel> comparator) {
            _compare = comparator;
        }

        @Override
        public int compare(Graph<VLabel, ELabel>.Vertex V0,
                Graph<VLabel, ELabel>.Vertex V1) {
            return _compare.compare(V0.getLabel(), V1.getLabel());
        }

        /** Stores a Comparator<VLabel> that will compare vertices.*/
        private Comparator<VLabel> _compare;
    }

    /** If the traversal ends prematurely, returns the Vertex argument to
     *  preVisit that caused a Visit routine to return false.  Otherwise,
     *  returns null. */
    public Graph<VLabel, ELabel>.Vertex finalVertex() {
        return _finalVertex;
    }

    /** If the traversal ends prematurely, returns the Edge argument to
     *  preVisit that caused a Visit routine to return false. If it was not
     *  an edge that caused termination, returns null. */
    public Graph<VLabel, ELabel>.Edge finalEdge() {
        return _finalEdge;
    }

    /** Returns the graph currently being traversed.  Undefined if no traversal
     *  is in progress. */
    protected Graph<VLabel, ELabel> theGraph() {
        return _graph;
    }

    /** Method to be called when adding the node at the other end of E from V0
     *  to the fringe. If this routine throws a StopException,
     *  the traversal ends.  If it throws a RejectException, the edge
     *  E is not traversed. The default does nothing.
     */
    protected void preVisit(Graph<VLabel, ELabel>.Edge e,
                            Graph<VLabel, ELabel>.Vertex v0) {
    }

    /** Method to be called when visiting vertex V.  If this routine throws
     *  a StopException, the traversal ends.  If it throws a RejectException,
     *  successors of V do not get visited from V. The default does nothing. */
    protected void visit(Graph<VLabel, ELabel>.Vertex v) {
    }

    /** Method to be called immediately after finishing the traversal
     *  of successors of vertex V in pre- and post-order traversals.
     *  If this routine throws a StopException, the traversal ends.
     *  Throwing a RejectException has no effect. The default does nothing.
     */
    protected void postVisit(Graph<VLabel, ELabel>.Vertex v) {
    }

    /** The Vertex (if any) that terminated the last traversal. */
    protected Graph<VLabel, ELabel>.Vertex _finalVertex;
    /** The Edge (if any) that terminated the last traversal. */
    protected Graph<VLabel, ELabel>.Edge _finalEdge;
    /** The last graph traversed. */
    protected Graph<VLabel, ELabel> _graph;

    /** Arraylist that keeps track of visited. */
    private ArrayList<Graph<VLabel, ELabel>.Vertex> _visited = new
            ArrayList<Graph<VLabel, ELabel>.Vertex>();

    /** True if the last traversal is depth first. */
    private boolean _depthsearch = false;

    /** True if the last traversal done is breadth. */
    private boolean _breadthsearch = false;

    /** True if the last traversal done is traversal. */
    private boolean _traversalsearch = false;

    /** A list used to keep track of vertices that have been previsited when
     *  using breadthFirstTraverse. */
    private ArrayList<Graph<VLabel, ELabel>.Vertex> _breadthpre = new
            ArrayList<Graph<VLabel, ELabel>.Vertex>();
    /** A list used to keep track of when to postvisit when using
     *  breadthFirstTraverse. */
    private ArrayList<Graph<VLabel, ELabel>.Vertex> _breadthpost = new
            ArrayList<Graph<VLabel, ELabel>.Vertex>();

    /** A list used to keep track of vertices that have been postvisited when
     *  using depthFirstTraverse. */
    private ArrayList<Graph<VLabel, ELabel>.Vertex> _depthpost = new
            ArrayList<Graph<VLabel, ELabel>.Vertex>();
    /** A Comparator<VLabel> that compares vertex labels in regular
     *  traversals. */
    private Comparator<VLabel> _comparer;
}

