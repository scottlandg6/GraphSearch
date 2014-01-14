package trip;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

import graph.DirectedGraph;
import graph.Distancer;
import graph.Graphs;
import graph.Weightable;
import graph.Weighted;
import graph.Graph;
/** Initial class for the 'trip' program.
 *  @author Scott Lee
 */
public final class Main {

    /** Entry point for the CS61B trip program.  ARGS may contain options
     *  and targets:
     *      [ -m MAP ] [ -o OUT ] [ REQUEST ]
     *  where MAP (default Map) contains the map data, OUT (default standard
     *  output) takes the result, and REQUEST (default standard input) contains
     *  the locations along the requested trip.
     */
    public static void main(String... args) {
        String mapFileName;
        String outFileName;
        String requestFileName;

        mapFileName = "Map";
        outFileName = requestFileName = null;

        int a;
        for (a = 0; a < args.length; a += 1) {
            if (args[a].equals("-m")) {
                a += 1;
                if (a == args.length) {
                    usage();
                } else {
                    mapFileName = args[a];
                }
            } else if (args[a].equals("-o")) {
                a += 1;
                if (a == args.length) {
                    usage();
                } else {
                    outFileName = args[a];
                }
            } else if (args[a].startsWith("-")) {
                usage();
            } else {
                break;
            }
        }

        if (a == args.length - 1) {
            requestFileName = args[a];
        } else if (a > args.length) {
            usage();
        }

        if (requestFileName != null) {
            try {
                System.setIn(new FileInputStream(requestFileName));
            } catch  (FileNotFoundException e) {
                System.err.printf("Could not open %s.%n", requestFileName);
                System.exit(1);
            }
        }

        if (outFileName != null) {
            try {
                System.setOut(new PrintStream(new FileOutputStream(outFileName),
                                              true));
            } catch  (FileNotFoundException e) {
                System.err.printf("Could not open %s for writing.%n",
                                  outFileName);
                System.exit(1);
            }
        }

        trip(mapFileName);
    }
    /** Represents the place. */
    private static class Place implements Weightable {
        /** Place takes in a NAME, X, and Y. */
        Place(String name, double x, double y) {
            _name = name;
            _x = x;
            _y = y;
        }

        /** Returns name. */
        public String getName() {
            return _name;
        }

        /** Returns coordinate x. */
        public double getx() {
            return _x;
        }

        /** Returns coordinate y. */
        public double gety() {
            return _y;
        }

        /**SetWeight. Takes in W. */
        @Override
        public void setWeight(double w) {
            _weight = w;
        }

        /** Returns WEIGHT. */
        @Override
        public double weight() {
            return _weight;
        }

        /** Place name. */
        private final String _name;
        /** Coordinates. */
        private final double _x, _y;
        /** WEIGHT. */
        private double _weight;
    }
    /** Represents the road. */
    private static class Road implements Weighted {
        /** Road takes in START, END, NAME, DISTANCE, AND DIRECTION. */
        Road(String start, String name, float distance, String direction,
                String end) {
            _start = start;
            _name = name;
            _distance = distance;
            _direction = direction;
            _end = end;
        }
        /** Returns Start. */
        public String start() {
            return _start;
        }
        /**Returns Name. */
        public String name() {
            return _name;
        }
        /**Returns Distance. */
        public float distance() {
            return _distance;
        }
        /**Returns Direction. */
        public String direction() {
            return _direction;
        }
        /**Returns End. */
        public String end() {
            return _end;
        }
        /**Returns Weigted. */
        @Override
        public double weight() {
            return _distance;
        }
        /** Start, end, name, and direction. */
        private final String _name, _direction;
        /** Distance. */
        private final float _distance;
        /** Start and End. */
        private final String _start, _end;
    }
    /** Print a trip for the request on the standard input to the standard
     *  output, using the map data in MAPFILENAME.
     */
    private static void trip(String mapFileName) {
        _places = new HashMap<String, Graph<Place, Road>.Vertex>();
        mapFile(mapFileName);
        Scanner requests = new Scanner(System.in);
        while (requests.hasNextLine()) {
            String request = requests.nextLine();
            requestor(request);
        }
        currStep = 0;
    }
    /** Read FILE and save roads and places. */
    private static void mapFile(String file) {
        String[] current;
        try {
            FileReader mapper = new FileReader(file);
            BufferedReader inp = new BufferedReader(mapper);
            while (true) {
                currInp.clear();
                currInput = mapFilehelper(inp);
                if (currInput == null) {
                    return;
                } else {
                    current = currInput.split(" ");
                }
                if (current[0].equals("L")) {
                    if (!_places.containsKey(current[1])) {
                        Graph<Main.Place, Main.Road>.Vertex a =
                                tripgraph.add(new Place(current[1],
                                        Float.parseFloat(current[2]),
                                        Float.parseFloat(current[3])));
                        _places.put(current[1], a);
                    }
                } else if (current[0].equals("R")) {
                    if (_places.containsKey(current[1])
                        && _places.containsKey(current[5])) {
                        direction(current[4]);
                        tripgraph.add(_places.get(current[1]),
                                _places.get(current[5]),
                                new Road(current[1], current[2],
                                        Float.parseFloat(current[3]),
                                        direct, current[5]));
                        tripgraph.add(_places.get(current[5]),
                                _places.get(current[1]),
                                new Road(current[5], current[2],
                                        Float.parseFloat(current[3]),
                                        oppositedir, current[1]));
                    } else {
                        System.out.println("Bad inputs.");
                        System.exit(1);
                    }
                }
            }
        } catch (FileNotFoundException x) {
            System.out.println("File doesn't exist");
            System.exit(1);
        }
    }
    /** Does directions. Takes in DIRECTION and gives the opposite.*/
    protected static void direction(String direction) {
        if (direction.equals("NS")) {
            direct = "south";
            oppositedir = "north";
        } else if (direction.equals("SN")) {
            direct = "north";
            oppositedir = "south";
        } else if (direction.equals("EW")) {
            direct = "west";
            oppositedir = "east";
        } else if (direction.equals("WE")) {
            direct = "east";
            oppositedir = "west";
        }
    }
    /** Takes in INP and returns valid input lines. */
    private static String mapFilehelper(BufferedReader inp) {
        String determiner = "";
        String result = "";
        boolean first = true;
        String current = "";
        try {
            for (int x = inp.read(); x != -1;) {
                current = Character.toString((char) x);
                if (determiner.equals("R")) {
                    if (currInp.size() == 6
                            && result.matches("R\\s+[\\p{Alnum}_-]+\\s+"
                                + "[\\p{Alnum}_-]+\\s+[\\d.]+\\s+[A-Z]+\\s+"
                                + "[\\p{Alnum}_-]+")) {
                        return result.replace("\\s+", " ");
                    }
                    if (currInp.size() > 6) {
                        throw new IOException();
                    }
                } else if (determiner.equals("L")) {
                    if (currInp.size() == 4 && result.matches("L\\s+[\\p{"
                            + "Alnum}_-]+\\s+-?[\\d.]+\\s+-?[\\d.]+")) {
                        return result.replace("\\s+", " ");
                    }
                    if (currInp.size() > 4) {
                        throw new IOException();
                    }
                } else if (!first) {
                    throw new IOException();
                }
                if (current.matches("\\s") && result.equals("")) {
                    x = inp.read();
                    continue;
                } else if (current.matches("\\s") && !result.equals("")) {
                    if (add) {
                        currInp.add(result);
                        add = false;
                    }
                    if ((determiner.equals("L") && currInp.size() == 4)
                            || determiner.equals("R") && currInp.size() == 6) {
                        continue;
                    } else {
                        result += " ";
                    }
                } else {
                    add = true;
                    if (first) {
                        determiner = current;
                        first = false;
                    }
                    result += current;
                }
                x = inp.read();
            }
        } catch (IOException x) {
            System.out.println("Error");
            System.exit(1);
        }
        return null;
    }
    /** Takes in requests and searches the graph for the input.
     * Takes in REQUEST*/
    private static void requestor(String request) {
        currentArray = request.trim().split(",\\s+");
        for (int x = 0; x < currentArray.length - 1; x++) {
            path = Graphs.shortestPath(tripgraph, _places.get(currentArray[x]),
                    _places.get(currentArray[x + 1]), TRIP_DIST);
            if (path == null) {
                System.out.println("Your request is invalid.");
                System.exit(1);
            }
            if (x == 0) {
                System.out.printf("From %s:\n\n", currentArray[x]);
            }
            Road next;
            for (int y = 0; y < path.size(); y++) {
                Road current = (Road) path.get(y).getLabel();
                float combinedlength = current.distance();
                int a = y;
                for (; a < path.size(); a++) {
                    if (a + 1 < path.size()) {
                        next = (Road) path.get(a + 1).getLabel();
                        if (next.name().equals(current.name())
                                && next.direction().equals(
                                        current.direction())) {
                            combinedlength = combinedlength + next.distance();
                            y++;
                        } else {
                            break;
                        }
                    }
                }
                if (y == path.size() - 1) {
                    System.out.printf("%d. Take %s %s for %.1f miles to "
                            + "%s.\n", currStep, current.name(),
                            current.direction(), combinedlength,
                            currentArray[x + 1]);
                    currStep++;
                    continue;
                } else {
                    System.out.printf("%d. Take %s %s for %.1f miles.\n",
                            currStep, current.name(), current.direction(),
                            combinedlength);
                }
                currStep++;
            }
        }
    }
    /** A tripdistancer. Return distance between V0 and V1. */
    public static final Distancer<Place> TRIP_DIST = new Distancer<Place>() {
        @Override
        public double dist(Place v0, Place v1) {
            return Math.pow(Math.pow(v1.getx() - v0.getx(), 2)
                    + Math.pow(v1.gety() - v0.gety(), 2), 0.5);
        }
    };

    /** Print a brief usage message and exit program abnormally. */
    private static void usage() {
        System.out.println("Entry point for the CS61B trip program."
                + " ARGS may contain options and targets:"
                + "     [ -m MAP ] [ -o OUT ] [ REQUEST ] "
                + "where MAP (default Map) contains the map data, OUT (default "
                + "standard output) takes the result, and REQUEST (default "
                + "standard input) contains the locations along the requested"
                + " trip.");
        System.exit(1);
    }
    /**Array to hold strings. */
    private static String[] currentArray;
    /** The path. */
    private static List<Graph<Place, Road>.Edge> path;
    /** An undirected graph that contains locations and distances. */
    private static Graph<Place, Road> tripgraph =
        new DirectedGraph<Place, Road>();
    /** A HashMap that maps the string form of a location to its actual
     *  Location class instance variable. */
    private static HashMap<String,
        Graph<Place, Road>.Vertex> _places;
    /** A String that holds the current input line from the map file. */
    private static String currInput;
    /**String for direction.*/
    private static String direct = "";
    /** String for opposite of direction. */
    private static String oppositedir = "";
    /** An int that keeps track of the current step number. */
    private static int currStep = 1;

    /** An ArrayList of strings that keeps track of the size of the current
     *  input line being read. */
    private static ArrayList<String> currInp = new ArrayList<String>();
    /** A boolean that is used in mapReaderHelper that is true iff it is time
     *  to increment the arrayList currInp by one. */
    private static boolean add = false;
}
