package make;

import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import graph.Graph;
import graph.Iteration;
import graph.DirectedGraph;
import java.util.Iterator;
import graph.Traversal;
import graph.RejectException;
import java.util.List;
import java.util.HashMap;
import java.util.Scanner;

/** Initial class for the 'make' program.
 *  @author Scott Lee
 */
public final class Main {

    /** Entry point for the CS61B make program.  ARGS may contain options
     *  and targets:
     *      [ -f MAKEFILE ] [ -D FILEINFO ] TARGET1 TARGET2 ...
     */
    public static void main(String... args) {
        String makefileName;
        String fileInfoName;

        if (args.length == 0) {
            usage();
        }

        makefileName = "Makefile";
        fileInfoName = "fileinfo";

        int a;
        for (a = 0; a < args.length; a += 1) {
            if (args[a].equals("-f")) {
                a += 1;
                if (a == args.length) {
                    usage();
                } else {
                    makefileName = args[a];
                }
            } else if (args[a].equals("-D")) {
                a += 1;
                if (a == args.length) {
                    usage();
                } else {
                    fileInfoName = args[a];
                }
            } else if (args[a].startsWith("-")) {
                usage();
            } else {
                break;
            }
        }

        ArrayList<String> targets = new ArrayList<String>();

        for (; a < args.length; a += 1) {
            targets.add(args[a]);
        }

        make(makefileName, fileInfoName, targets);
    }

    /** Carry out the make procedure using MAKEFILENAME as the makefile,
     *  taking information on the current file-system state from FILEINFONAME,
     *  and building TARGETS, or the first target in the makefile if TARGETS
     *  is empty.
     */
    private static void make(String makefileName, String fileInfoName,
                             List<String> targets) {
        try {
            Scanner makescan = new Scanner(new File(makefileName));
            Scanner filescan = new Scanner(new File(fileInfoName));
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            boolean first = true;
            ArrayList<Rule> rules = new ArrayList<Rule>();
            while (filescan.hasNextLine()) {
                String line = filescan.nextLine();
                String[] array = line.split("\\s+");
                if (first) {
                    first = false;
                } else {
                    map.put(array[0], Integer.parseInt(array[1]));
                }
            }
            while (makescan.hasNextLine()) {
                String string = makescan.nextLine();
                if (string.startsWith("#") || string.matches("\\s+")) {
                    continue;
                } else {
                    makescanner(string, rules);
                }
            }
            rules = refine(rules);
            exists(rules, map, targets);
            _graph = graph(rules);
            if (!targets.isEmpty()) {
                traversalhelper(_graph, targets, rules, map);
            } else {
                targets = new ArrayList<String>();
                targets.add(rules.get(0).getTarget());
                traversalhelper(_graph, targets, rules, map);
            }
        } catch (IOException err) {
            usage();
        }
    }

    /** Makescanner. Takes in STRING and RULES. */
    public static void makescanner(String string, ArrayList<Rule> rules) {
        if (!(string.startsWith(" "))) {
            String[] splitter = string.split("\\s+");
            splitter[0] = splitter[0].replace(":", "");
            if (splitter[0].matches(".*[:=\\#].*")) {
                System.err.println(": = # \\ are not allowed"
                    + "in the target.");
                System.exit(1);
            }
            rules.add(new Rule(splitter[0], new ArrayList<String>(),
                new ArrayList<String>()));
            for (int x = 1; x < splitter.length; x++) {
                if (splitter[x].matches(".*[:=\\#].*")) {
                    System.err.println(": = # \\ are not allowed in"
                            + "the prereqs.");
                    System.exit(1);
                }
                rules.get(rules.size() - 1).getDependencies().add(
                        splitter[x]);
            }
        } else {
            rules.get(rules.size() - 1).getCommands().add(string);
        }
    }

    /** Conducts the traversal that executes the make with the Graph GRAPH,
     *  a ArrayList TARGETS, an ArrayList RULES, a HashMap HASH from make. */
    public static void traversalhelper(Graph<String, String> graph,
            List<String> targets, ArrayList<Rule> rules, HashMap<String,
            Integer> hash) {
        ArrayList<Graph<String, String>.Vertex> list = new
            ArrayList<Graph<String, String>.Vertex>();
        Traverse<String, String> traverse
            = new Traverse<String, String>(graph, rules, hash, list);
        Graph<String, String>.Vertex curr = null;
        for (int i = 0; i < targets.size(); i += 1) {
            Iterator<Graph<String, String>.Vertex> verticesiteration
                = graph.vertices();
            while (verticesiteration.hasNext()) {
                Graph<String, String>.Vertex v = verticesiteration.next();
                if (v.getLabel().equals(targets.get(i))) {
                    curr = v;
                    break;
                }
            }
            traverse.depthFirstTraverse(graph, curr);
        }
    }

    /** Removes any repeating targets. Uses RULES. Returns Arraylist. */
    public static ArrayList<Rule> refine(ArrayList<Rule> rules) {
        ArrayList<Rule> copy = new ArrayList<Rule>();
        copy.addAll(rules);
        for (int x = 0; x < rules.size(); x++) {
            if (!(copy.contains(rules.get(x)))) {
                continue;
            }
            for (int y = x + 1; y < rules.size(); y++) {
                Rule one = rules.get(x);
                Rule two = rules.get(y);
                if (!(copy.contains(one))) {
                    break;
                } else if (!(copy.contains(two))) {
                    continue;
                }
                if (one.getTarget().equals(two.getTarget())) {
                    if (one.getCommands().isEmpty()) {
                        Rule newer = copy.get(copy.indexOf(two));
                        newer.getDependencies().addAll(one.getDependencies());
                        copy.remove(copy.indexOf(one));
                    } else if (two.getCommands().isEmpty()) {
                        Rule newer = copy.get(copy.indexOf(one));
                        newer.getDependencies().addAll(two.getDependencies());
                        copy.remove(copy.indexOf(two));
                    } else {
                        System.err.println("Error: two targets"
                            + "with separate non-empty command sets.");
                        System.exit(1);
                    }
                }
            }
        }
        return copy;
    }

    /** Checks if a Target exists by checking RULES, MAP, and a list
     * of TARGETS. */
    public static void exists(ArrayList<Rule> rules, HashMap<String, Integer>
        map, List<String> targets) {
        for (int x = 0; x < targets.size(); x++) {
            boolean checking = true;
            ArrayList<String> strings = new ArrayList<String>();
            String stringer = targets.get(x);
            for (int y = 0; y < rules.size(); y++) {
                if (stringer.equals(rules.get(y).getTarget())) {
                    strings = rules.get(y).getDependencies();
                    checking = false;
                    break;
                }
            }
            if (checking && !(map.containsKey(stringer))) {
                System.err.println("Target doesn't exist.");
                System.exit(1);
            }
            if (!(strings.isEmpty())) {
                for (int z = 0; z < strings.size(); z++) {
                    boolean checker = true;
                    String stringah = strings.get(z);
                    for (int b = 0; b < rules.size(); b++) {
                        if (stringah.equals(rules.get(b).getTarget())) {
                            strings = rules.get(b).getDependencies();
                            checker = false;
                            break;
                        }
                    }
                    if (checker && !(map.containsKey(stringah))) {
                        System.err.println("Target doesn't exist.");
                        System.exit(1);
                    }
                }
            }
        }
    }

    /** Creates a graph from RULES; used for traversals. Returns a graph.*/
    public static Graph<String, String> graph(ArrayList<Rule> rules) {
        Graph<String, String> grapher = new DirectedGraph<String, String>();
        for (int x = 0; x < rules.size(); x++) {
            boolean check = true;
            Graph<String, String>.Vertex v1 = null;
            Graph<String, String>.Vertex v2 = null;
            String string = rules.get(x).getTarget();
            Iterator<Graph<String, String>.Vertex> vertexiteration
                = grapher.vertices();
            while (vertexiteration.hasNext()) {
                Graph<String, String>.Vertex v = vertexiteration.next();
                if (v.getLabel().equals(string)) {
                    v1 = v;
                    check = false;
                    break;
                }
            }
            if (check) {
                Graph<String, String>.Vertex curr = grapher.add(string);
                v1 = curr;
            }
            for (int y = 0; y < rules.get(x).getDependencies().size(); y++) {
                check = true;
                String stringer = rules.get(x).getDependencies().get(y);
                Iterator<Graph<String, String>.Vertex> vertexiterator
                    = grapher.vertices();
                while (vertexiterator.hasNext()) {
                    Graph<String, String>.Vertex v = vertexiterator.next();
                    if (v.getLabel().equals(stringer)) {
                        v2 = v;
                        check = false;
                        break;
                    }
                }
                if (check) {
                    Graph<String, String>.Vertex curr = grapher.add(stringer);
                    v2 = curr;
                }
                grapher.add(v1, v2);
            }
        }
        return grapher;
    }

    /** Print a brief usage message and exit program abnormally. */
    private static void usage() {
        System.err.println("Entry point for the CS61B make program.");
        System.exit(1);
    }

    /** A field that contains the graph of make. */
    protected static Graph<String, String> _graph;

    /** Create a type Rule for the main method in make. */
    public static class Rule {

        /** A constructor for the type Rule with a
         *  TARGET, DEPENDENCIES, and COMMANDS. */
        Rule(String target, ArrayList<String> dependencies, ArrayList<String>
            commands) {
            _target = target;
            _dependencies = dependencies;
            _commands = commands;
        }

        /** Returns the target. */
        public String getTarget() {
            return _target;
        }

        /** Returns the dependencies. */
        public ArrayList<String> getDependencies() {
            return _dependencies;
        }

        /** Returns the commands of the Rule. */
        public ArrayList<String> getCommands() {
            return _commands;
        }

        /** Target.*/
        private String _target;

        /** Dependencies. */
        private ArrayList<String> _dependencies;

        /** Commands. */
        private ArrayList<String> _commands;

    }

    /** A class that checks for cyclic makes. */
    public static class Cycle<VLabel, ELabel> extends
        Traversal<VLabel, ELabel> {

        /** Constructor takes in V. */
        Cycle(Graph<VLabel, ELabel>.Vertex v) {
            _v = v;
        }

        @Override
        protected void visit(Graph<VLabel, ELabel>.Vertex v) {
            if (v == _v) {
                System.err.println("There exists a cycle.");
                System.exit(1);
            }
        }

        /** A field that has the Vertex. */
        protected Graph<VLabel, ELabel>.Vertex _v;

    }

    /** A class that traverses the graph of targets and executes
     *  their commands. */
    public static class Traverse<VLabel, ELabel> extends
        Traversal<VLabel, ELabel> {

        /** The constructor for the class Traverse with the Graph GRAPH
         *  the ArrayList RULES, the HashMap MAP, and the ArrayList LIST. */
        Traverse(Graph<VLabel, ELabel> graph, ArrayList<Rule> rules,
            HashMap<String, Integer> map, ArrayList<Graph<VLabel,
            ELabel>.Vertex> list) {
            super();
            _G = graph;
            _rules = rules;
            _files = map;
            _list = list;
        }
        @Override
        protected void postVisit(Graph<VLabel, ELabel>.Vertex v) {
            Iterator<Rule> iter = _rules.iterator();
            ArrayList<String> commands = new ArrayList<String>();
            while (iter.hasNext()) {
                Rule rule = iter.next();
                if (rule.getTarget().equals(v.getLabel())) {
                    if (!(rule.getDependencies().isEmpty())
                        || !(_files.containsKey(rule.getTarget()))) {
                        Iterator<Integer> iterator = _files.values().iterator();
                        int num = 0;
                        while (iterator.hasNext()) {
                            int z = iterator.next();
                            if (z > num) {
                                num = z;
                            }
                        }
                        num++;
                        _files.put(rule.getTarget(), num);
                        commands = rule.getCommands();
                    }
                }
            }
            for (int x = 0; x < commands.size(); x++) {
                System.out.println(commands.get(x));
            }
        }

        @Override
        protected void visit(Graph<VLabel, ELabel>.Vertex v) {
            try {
                Cycle<VLabel, ELabel> cycling = new Cycle<VLabel, ELabel>(v);
                Iteration<Graph<VLabel, ELabel>.Vertex> iter = _G.successors(v);
                if (_list.contains(v)) {
                    throw new RejectException();
                }
                while (iter.hasNext()) {
                    Graph<VLabel, ELabel>.Vertex vertex = iter.next();
                    cycling.depthFirstTraverse(_G, vertex);
                }
            } catch (NullPointerException x) {
                System.out.println("");
            }
        }

        /** The Graph. */
        private Graph<VLabel, ELabel> _G;
        /** A field to contain the Rules in Make. */
        private ArrayList<Rule> _rules;
        /** A field to hold the files and their times. */
        private HashMap<String, Integer> _files;
        /** A field to keep track of the traversed vertices. */
        private ArrayList<Graph<VLabel, ELabel>.Vertex> _list;
    }
}

