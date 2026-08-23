import java.util.*;

// This is the upgraded TileGraph from Lab 4 with weighted edges and directivity selection

class WeightedEdge {

    private Tile myTile;
    private int penalty;

    WeightedEdge(Tile thisTile, int pen) {
        myTile = thisTile;
        penalty = pen;
    }

    public Tile getTile() {
        return myTile;
    }

    public int getPenalty() {
        return penalty;
    }

    public boolean hasTile(Tile thisTile) {
        return myTile.equals(thisTile); // shallow comparison by reference only
    }

    public boolean isEqual(WeightedEdge thisWEdge) {
        boolean result = myTile.isEqual(thisWEdge.getTile());
        result &= (penalty == thisWEdge.getPenalty());
        return result;
    }
}

public class TileGraph {

    private Map<Tile, LinkedList<WeightedEdge>> adjList;

    public TileGraph() {
        adjList = new HashMap<>();
    }

    public TileGraph(TileMap thisMap) {
        adjList = new HashMap<>();
        buildGraph(thisMap);
    }

    public void addVertex(Tile thisTile) {
        adjList.putIfAbsent(thisTile, new LinkedList<WeightedEdge>());
    }

    public void addEdge(Tile src, Tile dst, int penalty) {
        adjList.putIfAbsent(src, new LinkedList<WeightedEdge>());
        adjList.putIfAbsent(dst, new LinkedList<WeightedEdge>()); // destination exists
        LinkedList<WeightedEdge> edges = adjList.get(src);
        for (WeightedEdge we : edges) {
            if (we.getTile().isEqual(dst)) {
                return;
            }
        }
        edges.add(new WeightedEdge(dst, penalty));
    }

    // returns the path with the smallest number of edges
    private LinkedList<Tile> UnweightedShortestPath(Tile start, Tile end) {
        LinkedList<Tile> queue = new LinkedList<>();
        HashSet<Tile> visited = new HashSet<>();
        HashMap<Tile, Tile> parentMap = new HashMap<>();

        queue.add(start);
        visited.add(start);
        parentMap.put(start, null);

        while (!queue.isEmpty()) {
            Tile current = queue.poll();

            if (current.isEqual(end)) {
                break;
            }

            LinkedList<WeightedEdge> neighbors = adjList.get(current);
            if (neighbors != null) {
                for (WeightedEdge we : neighbors) {
                    Tile neighbor = we.getTile();
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parentMap.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        LinkedList<Tile> reversedPath = new LinkedList<>();
        if (parentMap.containsKey(end)) {
            Tile curr = end;
            while (curr != null) {
                reversedPath.add(curr);
                curr = parentMap.get(curr);
            }
        }
        return reversedPath;
    }

    // dijkastra for path with lowest penalties
    private LinkedList<Tile> DijkstraShortestPath(Tile start, Tile end) {
        HashMap<Tile, Integer> dist = new HashMap<>();
        HashMap<Tile, Tile> parentMap = new HashMap<>();
        HashSet<Tile> unvisited = new HashSet<>(adjList.keySet());

        for (Tile t : adjList.keySet()) {
            dist.put(t, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        parentMap.put(start, null);

        while (!unvisited.isEmpty()) {
            Tile u = null;
            int minDist = Integer.MAX_VALUE;

            // Extract Min using linear Search due to small graph size
            for (Tile t : unvisited) {
                if (dist.get(t) < minDist) {
                    minDist = dist.get(t);
                    u = t;
                }
            }
            if (u == null)
                break; // unreachable nodes remain
            unvisited.remove(u);
            if (u.isEqual(end))
                break; // Found shortest path
            if (adjList.containsKey(u)) {
                for (WeightedEdge we : adjList.get(u)) {
                    Tile v = we.getTile();
                    if (!unvisited.contains(v))
                        continue;
                    int alt = dist.get(u) + we.getPenalty();
                    if (alt < dist.get(v)) {
                        dist.put(v, alt);
                        parentMap.put(v, u);
                    }
                }
            }
        }

        LinkedList<Tile> reversedPath = new LinkedList<>();
        if (parentMap.containsKey(end)) {
            Tile curr = end;
            while (curr != null) {
                reversedPath.add(curr);
                curr = parentMap.get(curr);
            }
        }
        return reversedPath;
    }

    // Bellman-Ford Lowest Penalty Path - returns null if negative weight cycles are
    // detected
    private LinkedList<Tile> BellmanShortestPath(Tile start, Tile end) {
        HashMap<Tile, Integer> dist = new HashMap<>();
        HashMap<Tile, Tile> parentMap = new HashMap<>();

        for (Tile t : adjList.keySet()) {
            dist.put(t, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        parentMap.put(start, null);

        int V = adjList.size();

        // Relax all edges V - 1 times
        for (int i = 1; i < V; i++) {
            for (Tile u : adjList.keySet()) {
                if (dist.get(u) != Integer.MAX_VALUE) {
                    for (WeightedEdge we : adjList.get(u)) {
                        Tile v = we.getTile();
                        int weight = we.getPenalty();
                        if (dist.get(u) + weight < dist.get(v)) {
                            dist.put(v, dist.get(u) + weight);
                            parentMap.put(v, u);
                        }
                    }
                }
            }
        }

        // Check for negative weight cycles
        for (Tile u : adjList.keySet()) {
            if (dist.get(u) != Integer.MAX_VALUE) {
                for (WeightedEdge we : adjList.get(u)) {
                    Tile v = we.getTile();
                    int weight = we.getPenalty();
                    if (dist.get(u) + weight < dist.get(v)) {
                        System.out.println("Warning: Negative weight cycle detected.");
                        return null;
                    }
                }
            }
        }

        LinkedList<Tile> reversedPath = new LinkedList<>();
        if (parentMap.containsKey(end)) {
            Tile curr = end;
            while (curr != null) {
                reversedPath.add(curr);
                curr = parentMap.get(curr);
            }
        }
        return reversedPath;
    }

    private LinkedList<Tile> DAGShortestPath(Tile start, Tile end) {
        LinkedList<Tile> topOrder = topologicalSort();
        if (topOrder == null)
            return new LinkedList<>(); // Not a valid DAG

        HashMap<Tile, Integer> dist = new HashMap<>();
        HashMap<Tile, Tile> parentMap = new HashMap<>();

        for (Tile t : adjList.keySet()) {
            dist.put(t, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        parentMap.put(start, null);

        for (Tile u : topOrder) {
            if (dist.get(u) != Integer.MAX_VALUE) {
                if (adjList.containsKey(u)) {
                    for (WeightedEdge we : adjList.get(u)) {
                        Tile v = we.getTile();
                        int weight = we.getPenalty();
                        if (dist.get(v) > dist.get(u) + weight) {
                            dist.put(v, dist.get(u) + weight);
                            parentMap.put(v, u);
                        }
                    }
                }
            }
        }

        LinkedList<Tile> reversedPath = new LinkedList<>();
        if (parentMap.containsKey(end)) {
            Tile curr = end;
            while (curr != null) {
                reversedPath.add(curr);
                curr = parentMap.get(curr);
            }
        }
        return reversedPath;
    }

    // Kahn's Topological Sorting
    private LinkedList<Tile> topologicalSort() // helper method for DAG Shortest Path
    {
        LinkedList<Tile> sortedList = new LinkedList<>();
        HashMap<Tile, Integer> inDegree = new HashMap<>();

        // Initialize in-degrees
        for (Tile u : adjList.keySet()) {
            inDegree.putIfAbsent(u, 0);
            for (WeightedEdge we : adjList.get(u)) {
                Tile v = we.getTile();
                inDegree.put(v, inDegree.getOrDefault(v, 0) + 1);
            }
        }

        Queue<Tile> queue = new LinkedList<>();
        for (Tile u : inDegree.keySet()) {
            if (inDegree.get(u) == 0) {
                queue.add(u);
            }
        }

        while (!queue.isEmpty()) {
            Tile u = queue.poll();
            sortedList.add(u);

            if (adjList.containsKey(u)) {
                for (WeightedEdge we : adjList.get(u)) {
                    Tile v = we.getTile();
                    int inDeg = inDegree.get(v) - 1;
                    inDegree.put(v, inDeg);
                    if (inDeg == 0) {
                        queue.add(v);
                    }
                }
            }
        }

        // If sorting handled all vertices it's valid dag, otherwise there's a cycle
        return sortedList.size() == adjList.size() ? sortedList : null;
    }

    public LinkedList<Tile> findShortestPath(Tile start, Tile end, int complexity) {
        switch (complexity) {
            default:
            case 0:
                return UnweightedShortestPath(start, end); // Lab 4

            case 1:
                return DAGShortestPath(start, end); // DAG

            case 2:
                return DijkstraShortestPath(start, end); // Dijkstra

            case 3:
                return BellmanShortestPath(start, end); // Bellman
        }
    }

    public void printGraph() {
        Set<Tile> keySet = adjList.keySet();
        Collection<LinkedList<WeightedEdge>> valueLists = adjList.values();

        Iterator<Tile> keySetIter = keySet.iterator(); // so to iterate through map
        Iterator<LinkedList<WeightedEdge>> valueListsIter = valueLists.iterator(); // so to iterate through map
        int size = keySet.size();

        for (int i = 0; i < size; i++) {
            keySetIter.next().printTileCoord();
            System.out.printf(" >>\t");
            valueListsIter.next().forEach(e -> {
                e.getTile().printTileCoord();
                System.out.printf(" : ");
            });
            System.out.println();
        }
    }

    private void buildGraph(TileMap mapRef) {
        // HIDDEN - Completed in Lab 3

        // You must upgrade your lab 4 code so that the following weights are scanned
        // into the edge of the graph
        // Score Penalty ('x') gives a weight of +5
        // Score Reward ('$') gives a weight of -2

        Tile[][] map = mapRef.getMapRef();
        LinkedList<Tile> queue = new LinkedList<>();
        HashSet<Tile> visited = new HashSet<>();

        Tile start = mapRef.getStartTile();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Tile current = queue.poll();
            this.addVertex(current);
            int currX = current.getX();
            int currY = current.getY();

            // Scan East (Right)
            int penalty = 0;
            for (int x = currX + 1; x < map[0].length; x++) {
                Tile next = map[currY][x];

                if (next.getTileType() == '#')
                    break;
                if (next.getTileType() == 'x') // penalty
                    penalty += 5;
                if (next.getTileType() == '$') // reward
                    penalty -= 2;

                if (next.getTileType() == 'I' || next.getTileType() == 'D') {
                    this.addVertex(next);
                    this.addEdge(current, next, penalty);

                    if (!visited.contains(next)) {
                        visited.add(next);
                        queue.add(next);
                    }
                    break;
                }
            }

            // Scan South (Down)
            penalty = 0;
            for (int y = currY + 1; y < map.length; y++) {
                Tile next = map[y][currX];

                if (next.getTileType() == '#')
                    break;
                if (next.getTileType() == 'x')
                    penalty += 5;
                if (next.getTileType() == '$')
                    penalty -= 2;

                if (next.getTileType() == 'I' || next.getTileType() == 'D') {
                    this.addVertex(next);
                    this.addEdge(current, next, penalty);

                    if (!visited.contains(next)) {
                        visited.add(next);
                        queue.add(next);
                    }
                    break;
                }
            }
        }
    }

    // Helper method to setup custom graphs
    private static Tile[] createCustomTestGraph(TileGraph testGraph) {
        Tile[] t = new Tile[8];
        for (int i = 0; i < 8; i++) {
            t[i] = new Tile(i, i, 'I', -5);
            testGraph.addVertex(t[i]);
        }
        return t;
    }

    // Helper method to populate 12-edge network
    private static void populateCustomEdges(TileGraph testGraph, Tile[] t, boolean hasNegative) {
        testGraph.addEdge(t[0], t[1], 1);
        testGraph.addEdge(t[0], t[2], 5);
        testGraph.addEdge(t[0], t[3], 5);
        testGraph.addEdge(t[1], t[3], 5);
        testGraph.addEdge(t[1], t[4], 1);
        testGraph.addEdge(t[2], t[4], 5);
        testGraph.addEdge(t[2], t[5], 5);
        testGraph.addEdge(t[2], t[7], 10);
        testGraph.addEdge(t[3], t[6], 5);
        testGraph.addEdge(t[4], t[6], 5);
        testGraph.addEdge(t[4], t[7], hasNegative ? -2 : 1);
        testGraph.addEdge(t[5], t[7], 5);
    }

    // Test Bench Below
    // Test Bench Below
    // Test Bench Below

    private static boolean totalPassed = true;
    private static int totalTestCount = 0;
    private static int totalPassCount = 0;

    public static void main(String args[]) {
        testAddWeightedEdge1();
        testAddWeightedEdge2();
        testAddWeightedEdgeCustom();

        testTopologicalSort1();
        testTopologicalSort2();
        testTopologicalSortCustom();

        testShortestPathDAG1();
        testShortestPathDAG2();
        testShortestPathDAGCustom();

        testShortestPathDijkastra1();
        testShortestPathDijkastra2();
        testShortestPathDijkastraCustom();

        testShortestPathBellmanFord1();
        testShortestPathBellmanFord2();
        testNegativeCycleBellmanFord1();
        testNegativeCycleBellmanFord2();
        testShortestPathBellmanFordCustom();

        System.out.println("================================");
        System.out.printf("Test Score: %d / %d\n",
                totalPassCount,
                totalTestCount);
        if (totalPassed) {
            System.out.println("All Tests Passed.");
            System.exit(0);
        } else {
            System.out.println("Tests Failed.");
            System.exit(-1);
        }
    }

    // Add Weighted Edges (Code Upgrade from Lab 3)
    // Add Weighted Edges (Code Upgrade from Lab 3)
    // Add Weighted Edges (Code Upgrade from Lab 3)

    private static void testAddWeightedEdge1() {
        // Setup
        System.out.println("============testAddWeightedEdge1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
                new Tile(0, 0, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 4), // 0
                new WeightedEdge(tileArray[2], 2), // 0
                new WeightedEdge(tileArray[4], 8), // 1
                new WeightedEdge(tileArray[3], 7), // 1
                new WeightedEdge(tileArray[3], 2), // 2
                new WeightedEdge(tileArray[4], 1) // 3
        };

        for (int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        // Action
        LinkedList<WeightedEdge> tempList;
        boolean tempResult;

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();

        tempList = testGraph.adjList.get(tileArray[0]);
        passed &= assertEquals(2, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[4]) ||
                    testWEdge.isEqual(wEdge[5]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[0]))
                passed &= assertEquals(wEdge[0].getPenalty(), testWEdge.getPenalty());
            else if (testWEdge.isEqual(wEdge[1]))
                passed &= assertEquals(wEdge[1].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[1]);
        passed &= assertEquals(2, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[4]) ||
                    testWEdge.isEqual(wEdge[5]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[3]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[2]))
                passed &= assertEquals(wEdge[2].getPenalty(), testWEdge.getPenalty());
            else if (testWEdge.isEqual(wEdge[3]))
                passed &= assertEquals(wEdge[3].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[2]);
        passed &= assertEquals(1, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[5]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[4]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[4]))
                passed &= assertEquals(wEdge[4].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[3]);
        passed &= assertEquals(1, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[4]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[5]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[5]))
                passed &= assertEquals(wEdge[5].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[4]);
        passed &= assertEquals(true, tempList.isEmpty());

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testAddWeightedEdge2() {
        // Setup
        System.out.println("============testAddWeightedEdge2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = { new Tile(0, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(4, 8, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5),
                new Tile(10, 16, 'I', -5),
                new Tile(10, 23, 'I', -5) };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 4), // 0
                new WeightedEdge(tileArray[2], 2), // 0
                new WeightedEdge(tileArray[3], 8), // 0
                new WeightedEdge(tileArray[4], 3), // 1
                new WeightedEdge(tileArray[5], 1), // 1
                new WeightedEdge(tileArray[3], 4), // 2
                new WeightedEdge(tileArray[4], 2), // 2
                new WeightedEdge(tileArray[6], 6), // 3
                new WeightedEdge(tileArray[5], 5), // 4
                new WeightedEdge(tileArray[6], 1), // 5
                new WeightedEdge(tileArray[7], 3), // 5
                new WeightedEdge(tileArray[7], 1) // 6
        };

        for (int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        // Action
        LinkedList<WeightedEdge> tempList;
        boolean tempResult;

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[0].printTileCoord();
        System.out.println();

        tempList = testGraph.adjList.get(tileArray[0]);
        passed &= assertEquals(3, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[4]) ||
                    testWEdge.isEqual(wEdge[5]) ||
                    testWEdge.isEqual(wEdge[6]) ||
                    testWEdge.isEqual(wEdge[7]) ||
                    testWEdge.isEqual(wEdge[8]) ||
                    testWEdge.isEqual(wEdge[9]) ||
                    testWEdge.isEqual(wEdge[10]) ||
                    testWEdge.isEqual(wEdge[11]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[2]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[0]))
                passed &= assertEquals(wEdge[0].getPenalty(), testWEdge.getPenalty());
            else if (testWEdge.isEqual(wEdge[1]))
                passed &= assertEquals(wEdge[1].getPenalty(), testWEdge.getPenalty());
            else if (testWEdge.isEqual(wEdge[2]))
                passed &= assertEquals(wEdge[2].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[1].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[1]);
        passed &= assertEquals(2, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[5]) ||
                    testWEdge.isEqual(wEdge[6]) ||
                    testWEdge.isEqual(wEdge[7]) ||
                    testWEdge.isEqual(wEdge[8]) ||
                    testWEdge.isEqual(wEdge[9]) ||
                    testWEdge.isEqual(wEdge[10]) ||
                    testWEdge.isEqual(wEdge[11]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[4]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[3]))
                passed &= assertEquals(wEdge[3].getPenalty(), testWEdge.getPenalty());
            else if (testWEdge.isEqual(wEdge[4]))
                passed &= assertEquals(wEdge[4].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[2].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[2]);
        passed &= assertEquals(2, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[4]) ||
                    testWEdge.isEqual(wEdge[7]) ||
                    testWEdge.isEqual(wEdge[8]) ||
                    testWEdge.isEqual(wEdge[9]) ||
                    testWEdge.isEqual(wEdge[10]) ||
                    testWEdge.isEqual(wEdge[11]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[5]) ||
                    testWEdge.isEqual(wEdge[6]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[5]))
                passed &= assertEquals(wEdge[5].getPenalty(), testWEdge.getPenalty());
            else if (testWEdge.isEqual(wEdge[6]))
                passed &= assertEquals(wEdge[6].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[3].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[3]);
        passed &= assertEquals(1, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[4]) ||
                    testWEdge.isEqual(wEdge[5]) ||
                    testWEdge.isEqual(wEdge[6]) ||
                    testWEdge.isEqual(wEdge[8]) ||
                    testWEdge.isEqual(wEdge[9]) ||
                    testWEdge.isEqual(wEdge[10]) ||
                    testWEdge.isEqual(wEdge[11]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[7]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[7]))
                passed &= assertEquals(wEdge[7].getPenalty(), testWEdge.getPenalty());

        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[4].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[4]);
        passed &= assertEquals(1, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[4]) ||
                    testWEdge.isEqual(wEdge[5]) ||
                    testWEdge.isEqual(wEdge[6]) ||
                    testWEdge.isEqual(wEdge[7]) ||
                    testWEdge.isEqual(wEdge[9]) ||
                    testWEdge.isEqual(wEdge[10]) ||
                    testWEdge.isEqual(wEdge[11]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[8]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[8]))
                passed &= assertEquals(wEdge[8].getPenalty(), testWEdge.getPenalty());

        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[5].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[5]);
        passed &= assertEquals(2, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[4]) ||
                    testWEdge.isEqual(wEdge[5]) ||
                    testWEdge.isEqual(wEdge[6]) ||
                    testWEdge.isEqual(wEdge[7]) ||
                    testWEdge.isEqual(wEdge[8]) ||
                    testWEdge.isEqual(wEdge[11]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[9]) ||
                    testWEdge.isEqual(wEdge[10]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[9]))
                passed &= assertEquals(wEdge[9].getPenalty(), testWEdge.getPenalty());
            else if (testWEdge.isEqual(wEdge[10]))
                passed &= assertEquals(wEdge[10].getPenalty(), testWEdge.getPenalty());
        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[6].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[6]);
        passed &= assertEquals(1, tempList.size());

        for (WeightedEdge testWEdge : tempList) {
            tempResult = testWEdge.isEqual(wEdge[0]) ||
                    testWEdge.isEqual(wEdge[1]) ||
                    testWEdge.isEqual(wEdge[2]) ||
                    testWEdge.isEqual(wEdge[3]) ||
                    testWEdge.isEqual(wEdge[4]) ||
                    testWEdge.isEqual(wEdge[5]) ||
                    testWEdge.isEqual(wEdge[6]) ||
                    testWEdge.isEqual(wEdge[7]) ||
                    testWEdge.isEqual(wEdge[8]) ||
                    testWEdge.isEqual(wEdge[9]) ||
                    testWEdge.isEqual(wEdge[10]);

            passed &= assertEquals(false, tempResult);

            tempResult = testWEdge.isEqual(wEdge[11]);

            passed &= assertEquals(true, tempResult);

            if (testWEdge.isEqual(wEdge[11]))
                passed &= assertEquals(wEdge[11].getPenalty(), testWEdge.getPenalty());

        }

        System.out.printf(">> Check Vertex Adjacency List: ");
        tileArray[7].printTileCoord();
        System.out.println();
        tempList = testGraph.adjList.get(tileArray[7]);
        passed &= assertEquals(true, tempList.isEmpty());

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testAddWeightedEdgeCustom() {
        // Setup
        System.out.println("============testAddWeightedEdgeCustom=============");
        boolean passed = true;
        totalTestCount++;

        // check that the graph correctly attaches and counts multiple weighted edges
        // for specific nodes without duplicates

        TileGraph testGraph = new TileGraph();
        Tile[] t = createCustomTestGraph(testGraph);
        populateCustomEdges(testGraph, t, false);

        passed &= assertEquals(3, testGraph.adjList.get(t[0]).size());
        passed &= assertEquals(2, testGraph.adjList.get(t[1]).size());
        passed &= assertEquals(3, testGraph.adjList.get(t[2]).size());

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    // Topological Sort
    // Topological Sort
    // Topological Sort

    private static void testTopologicalSort1() {
        // Setup
        System.out.println("============testTopologicalSort1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
                new Tile(0, 0, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 4), // 0
                new WeightedEdge(tileArray[2], 2), // 0
                new WeightedEdge(tileArray[4], 8), // 1
                new WeightedEdge(tileArray[3], 7), // 1
                new WeightedEdge(tileArray[3], 2), // 2
                new WeightedEdge(tileArray[4], 1) // 3
        };

        for (int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        // Action
        LinkedList<Tile> sorted = testGraph.topologicalSort();

        if (sorted != null) {
            // Valid solutions:
            // 1: 0,1,2,3,4
            // 2: 0,2,1,3,4
            boolean solution1 = sorted.get(0).equals(tileArray[0]) &&
                    ((sorted.get(1).equals(tileArray[1]) && sorted.get(2).equals(tileArray[2])) ||
                            (sorted.get(1).equals(tileArray[2]) && sorted.get(2).equals(tileArray[1])))
                    &&
                    sorted.get(3).equals(tileArray[3]) &&
                    sorted.get(4).equals(tileArray[4]);

            passed &= solution1;
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testTopologicalSort2() {
        // Setup
        System.out.println("============testTopologicalSort2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = { new Tile(0, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(4, 8, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5),
                new Tile(10, 16, 'I', -5),
                new Tile(10, 23, 'I', -5) };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 4), // 0
                new WeightedEdge(tileArray[2], 2), // 0
                new WeightedEdge(tileArray[3], 8), // 0
                new WeightedEdge(tileArray[4], 3), // 1
                new WeightedEdge(tileArray[5], 1), // 1
                new WeightedEdge(tileArray[3], 4), // 2
                new WeightedEdge(tileArray[4], 2), // 2
                new WeightedEdge(tileArray[6], 6), // 3
                new WeightedEdge(tileArray[5], 5), // 4
                new WeightedEdge(tileArray[6], 1), // 5
                new WeightedEdge(tileArray[7], 3), // 5
                new WeightedEdge(tileArray[7], 1) // 6
        };

        for (int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        // Action
        LinkedList<Tile> sorted = testGraph.topologicalSort();

        if (sorted != null) {
            // Check if the solution is one of the valid topological sorts
            boolean validSolution = false;

            if (sorted.get(0).equals(tileArray[0])) {

                int[] indexMap = new int[tileArray.length];
                for (int i = 0; i < sorted.size(); i++) {
                    for (int j = 0; j < tileArray.length; j++) {
                        if (sorted.get(i).equals(tileArray[j])) {
                            indexMap[j] = i;
                            break;
                        }
                    }
                }

                // Check all dependencies
                validSolution = true;

                validSolution &= indexMap[1] > indexMap[0];

                validSolution &= indexMap[2] > indexMap[0];

                validSolution &= indexMap[3] > indexMap[0];
                validSolution &= indexMap[3] > indexMap[2];

                validSolution &= indexMap[4] > indexMap[1];
                validSolution &= indexMap[4] > indexMap[2];

                validSolution &= indexMap[5] > indexMap[1];

                validSolution &= indexMap[6] > indexMap[3];
                validSolution &= indexMap[6] > indexMap[5];

                validSolution &= indexMap[7] > indexMap[5];
                validSolution &= indexMap[7] > indexMap[6];
            }

            passed &= validSolution;
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testTopologicalSortCustom() {
        // Setup
        System.out.println("============testTopologicalSortCustom=============");
        boolean passed = true;
        totalTestCount++;

        // check that kahn's algorithm processes and sorts a valid 8-node dag without
        // dropping vertices

        TileGraph testGraph = new TileGraph();
        Tile[] t = createCustomTestGraph(testGraph);
        populateCustomEdges(testGraph, t, false);

        LinkedList<Tile> sorted = testGraph.topologicalSort();
        passed &= (sorted != null && sorted.size() == 8);

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    // Shortest Path for Directed Acyclic Graph using Topological Sort
    // Shortest Path for Directed Acyclic Graph using Topological Sort
    // Shortest Path for Directed Acyclic Graph using Topological Sort

    private static void testShortestPathDAG1() {
        // Setup
        System.out.println("============testShortestPathDAG1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
                new Tile(0, 0, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 4), // 0
                new WeightedEdge(tileArray[2], 2), // 0
                new WeightedEdge(tileArray[4], 8), // 1
                new WeightedEdge(tileArray[3], 7), // 1
                new WeightedEdge(tileArray[3], 2), // 2
                new WeightedEdge(tileArray[4], 1) // 3
        };

        for (int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.DAGShortestPath(tileArray[0], tileArray[4]);

        if (path != null) {
            passed &= assertEquals(tileArray[4], path.get(0));
            passed &= assertEquals(tileArray[3], path.get(1));
            passed &= assertEquals(tileArray[2], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testShortestPathDAG2() {
        // Setup
        System.out.println("============testShortestPathDAG2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = { new Tile(0, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(4, 8, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5),
                new Tile(10, 16, 'I', -5),
                new Tile(10, 23, 'I', -5) };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 4), // 0
                new WeightedEdge(tileArray[2], 2), // 0
                new WeightedEdge(tileArray[3], 8), // 0
                new WeightedEdge(tileArray[4], 3), // 1
                new WeightedEdge(tileArray[5], 1), // 1
                new WeightedEdge(tileArray[3], 4), // 2
                new WeightedEdge(tileArray[4], 2), // 2
                new WeightedEdge(tileArray[6], 6), // 3
                new WeightedEdge(tileArray[5], 5), // 4
                new WeightedEdge(tileArray[6], 1), // 5
                new WeightedEdge(tileArray[7], 3), // 5
                new WeightedEdge(tileArray[7], 1) // 6
        };

        for (int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.DAGShortestPath(tileArray[0], tileArray[7]);

        if (path != null) {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[6], path.get(1));
            passed &= assertEquals(tileArray[5], path.get(2));
            passed &= assertEquals(tileArray[1], path.get(3));
            passed &= assertEquals(tileArray[0], path.get(4));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testShortestPathDAGCustom() {
        // Setup
        System.out.println("============testShortestPathDAGCustom=============");
        boolean passed = true;
        totalTestCount++;

        // Checks if the dag algorithm correctly uses topological sort

        TileGraph testGraph = new TileGraph();
        Tile[] t = createCustomTestGraph(testGraph);
        populateCustomEdges(testGraph, t, false);

        LinkedList<Tile> path = testGraph.DAGShortestPath(t[0], t[7]);

        if (path != null && path.size() > 0) {
            passed &= assertEquals(t[7], path.get(0));
            passed &= assertEquals(t[4], path.get(1));
            passed &= assertEquals(t[1], path.get(2));
            passed &= assertEquals(t[0], path.get(3));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    // Shortest Path using Dijkastra Algorithm on Positive-Weighted Directed Graph
    // Shortest Path using Dijkastra Algorithm on Positive-Weighted Directed Graph
    // Shortest Path using Dijkastra Algorithm on Positive-Weighted Directed Graph

    private static void testShortestPathDijkastra1() {
        // Setup
        System.out.println("============testShortestPathDijkastra1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
                new Tile(0, 0, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 4), // 0
                new WeightedEdge(tileArray[2], 6), // 0
                new WeightedEdge(tileArray[4], 5), // 1
                new WeightedEdge(tileArray[3], 1), // 1
                new WeightedEdge(tileArray[3], 10), // 2
                new WeightedEdge(tileArray[4], 7) // 3
        };

        for (int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.DijkstraShortestPath(tileArray[0], tileArray[4]);

        if (path != null) {
            passed &= assertEquals(tileArray[4], path.get(0));
            passed &= assertEquals(tileArray[1], path.get(1));
            passed &= assertEquals(tileArray[0], path.get(2));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testShortestPathDijkastra2() {
        // Setup
        System.out.println("============testShortestPathDijkastra2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = { new Tile(0, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(4, 8, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5),
                new Tile(10, 16, 'I', -5),
                new Tile(10, 23, 'I', -5) };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 5), // 0
                new WeightedEdge(tileArray[2], 4), // 0
                new WeightedEdge(tileArray[3], 2), // 0
                new WeightedEdge(tileArray[4], 7), // 1
                new WeightedEdge(tileArray[5], 11), // 1
                new WeightedEdge(tileArray[3], 3), // 2
                new WeightedEdge(tileArray[4], 5), // 2
                new WeightedEdge(tileArray[6], 1), // 3
                new WeightedEdge(tileArray[5], 2), // 4
                new WeightedEdge(tileArray[6], 7), // 5
                new WeightedEdge(tileArray[7], 6), // 5
                new WeightedEdge(tileArray[7], 10) // 6
        };

        for (int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.DijkstraShortestPath(tileArray[0], tileArray[7]);

        if (path != null) {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[6], path.get(1));
            passed &= assertEquals(tileArray[3], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testShortestPathDijkastraCustom() {
        // Setup
        System.out.println("============testShortestPathDijkastraCustom=============");
        boolean passed = true;
        totalTestCount++;

        // checks if dijkstra's greedy approach finds the same optimal path on the
        // custom network using only positive weights

        TileGraph testGraph = new TileGraph();
        Tile[] t = createCustomTestGraph(testGraph);
        populateCustomEdges(testGraph, t, false);

        LinkedList<Tile> path = testGraph.DijkstraShortestPath(t[0], t[7]);

        if (path != null && path.size() > 0) {
            passed &= assertEquals(t[7], path.get(0));
            passed &= assertEquals(t[4], path.get(1));
            passed &= assertEquals(t[1], path.get(2));
            passed &= assertEquals(t[0], path.get(3));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    // Shortest Path using Bellman-Ford Algorithm on General-Weighted Graph
    // Shortest Path using Bellman-Ford Algorithm on General-Weighted Graph
    // Shortest Path using Bellman-Ford Algorithm on General-Weighted Graph

    private static void testShortestPathBellmanFord1() {
        // Setup
        System.out.println("============testShortestPathBellmanFord1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
                new Tile(0, 0, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 2), // 0
                new WeightedEdge(tileArray[2], 4), // 0
                new WeightedEdge(tileArray[4], -3), // 1
                new WeightedEdge(tileArray[3], -4), // 1
                new WeightedEdge(tileArray[3], 3), // 2
                new WeightedEdge(tileArray[4], -1) // 3
        };

        for (int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[4]);

        if (path != null) {
            passed &= assertEquals(tileArray[4], path.get(0));
            passed &= assertEquals(tileArray[3], path.get(1));
            passed &= assertEquals(tileArray[1], path.get(2));
            passed &= assertEquals(tileArray[0], path.get(3));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testShortestPathBellmanFord2() {
        // Setup
        System.out.println("============testShortestPathBellmanFord2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = { new Tile(0, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(4, 8, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5),
                new Tile(10, 16, 'I', -5),
                new Tile(10, 23, 'I', -5) };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 5), // 0
                new WeightedEdge(tileArray[2], 4), // 0
                new WeightedEdge(tileArray[3], 2), // 0
                new WeightedEdge(tileArray[4], -3), // 1
                new WeightedEdge(tileArray[5], 11), // 1
                new WeightedEdge(tileArray[3], -2), // 2
                new WeightedEdge(tileArray[4], 5), // 2
                new WeightedEdge(tileArray[6], 1), // 3
                new WeightedEdge(tileArray[5], -4), // 4
                new WeightedEdge(tileArray[6], 7), // 5
                new WeightedEdge(tileArray[7], -1), // 5
                new WeightedEdge(tileArray[7], 10) // 6
        };

        for (int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[11].getTile(), wEdge[11].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[7]);

        if (path != null) {
            passed &= assertEquals(tileArray[7], path.get(0));
            passed &= assertEquals(tileArray[5], path.get(1));
            passed &= assertEquals(tileArray[4], path.get(2));
            passed &= assertEquals(tileArray[1], path.get(3));
            passed &= assertEquals(tileArray[0], path.get(4));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testNegativeCycleBellmanFord1() {
        // Setup
        System.out.println("============testNegativeCycleBellmanFord1=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = {
                new Tile(0, 0, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5)
        };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 2), // 0
                new WeightedEdge(tileArray[2], 4), // 0
                new WeightedEdge(tileArray[4], -3), // 1
                new WeightedEdge(tileArray[3], -4), // 1
                new WeightedEdge(tileArray[3], 3), // 2
                new WeightedEdge(tileArray[4], -1), // 3
                new WeightedEdge(tileArray[1], -4) // 4
        };

        for (int i = 0; i < 5; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[6].getTile(), wEdge[6].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[4]);
        passed &= assertEquals(true, path == null);

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testNegativeCycleBellmanFord2() {
        // Setup
        System.out.println("============testNegativeCycleBellmanFord2=============");
        boolean passed = true;
        totalTestCount++;

        TileGraph testGraph = new TileGraph();
        Tile tileArray[] = { new Tile(0, 0, 'I', -5),
                new Tile(0, 4, 'I', -5),
                new Tile(4, 0, 'I', -5),
                new Tile(4, 8, 'I', -5),
                new Tile(5, 5, 'I', -5),
                new Tile(5, 10, 'I', -5),
                new Tile(10, 16, 'I', -5),
                new Tile(10, 23, 'I', -5) };

        WeightedEdge wEdge[] = {
                new WeightedEdge(tileArray[1], 5), // 0
                new WeightedEdge(tileArray[2], 4), // 0
                new WeightedEdge(tileArray[3], 2), // 0
                new WeightedEdge(tileArray[4], -3), // 1
                new WeightedEdge(tileArray[5], 11), // 1
                new WeightedEdge(tileArray[3], -2), // 2
                new WeightedEdge(tileArray[4], 5), // 2
                new WeightedEdge(tileArray[6], 1), // 3
                new WeightedEdge(tileArray[5], -4), // 4
                new WeightedEdge(tileArray[6], 7), // 5
                new WeightedEdge(tileArray[1], 2), // 5
                new WeightedEdge(tileArray[7], -1), // 5
                new WeightedEdge(tileArray[7], 10) // 6
        };

        for (int i = 0; i < 8; i++)
            testGraph.addVertex(tileArray[i]);

        testGraph.addEdge(tileArray[0], wEdge[0].getTile(), wEdge[0].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[1].getTile(), wEdge[1].getPenalty());
        testGraph.addEdge(tileArray[0], wEdge[2].getTile(), wEdge[2].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[3].getTile(), wEdge[3].getPenalty());
        testGraph.addEdge(tileArray[1], wEdge[4].getTile(), wEdge[4].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[5].getTile(), wEdge[5].getPenalty());
        testGraph.addEdge(tileArray[2], wEdge[6].getTile(), wEdge[6].getPenalty());
        testGraph.addEdge(tileArray[3], wEdge[7].getTile(), wEdge[7].getPenalty());
        testGraph.addEdge(tileArray[4], wEdge[8].getTile(), wEdge[8].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[9].getTile(), wEdge[9].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[10].getTile(), wEdge[10].getPenalty());
        testGraph.addEdge(tileArray[5], wEdge[11].getTile(), wEdge[11].getPenalty());
        testGraph.addEdge(tileArray[6], wEdge[12].getTile(), wEdge[12].getPenalty());

        // Action
        LinkedList<Tile> path = testGraph.BellmanShortestPath(tileArray[0], tileArray[7]);
        passed &= assertEquals(true, path == null);

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    private static void testShortestPathBellmanFordCustom() {
        // Setup
        System.out.println("============testShortestPathBellmanFordCustom=============");
        boolean passed = true;
        totalTestCount++;

        // checks bellman-ford algorithm to find optimal path with penalty

        TileGraph testGraph = new TileGraph();
        Tile[] t = createCustomTestGraph(testGraph);
        populateCustomEdges(testGraph, t, true); // true to add a negative weighted edge

        LinkedList<Tile> path = testGraph.BellmanShortestPath(t[0], t[7]);

        if (path != null && path.size() > 0) {
            passed &= assertEquals(t[7], path.get(0));
            passed &= assertEquals(t[4], path.get(1));
            passed &= assertEquals(t[1], path.get(2));
            passed &= assertEquals(t[0], path.get(3));
        } else {
            passed = false;
        }

        // Tear Down
        totalPassed &= passed;
        if (passed) {
            System.out.println("\tPassed");
            totalPassCount++;
        }
    }

    ////// ASSERTIONS //////
    ////// ASSERTIONS //////
    ////// ASSERTIONS //////

    private static boolean assertEquals(Tile expected, Tile actual) {
        if (!expected.isEqual(actual)) {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected:");
            expected.printTile();
            expected.printTileCoord();
            System.out.printf("\tActual:");
            actual.printTile();
            actual.printTileCoord();
            return false;
        }

        return true;
    }

    private static boolean assertEquals(boolean expected, boolean actual) {
        if (expected != actual) {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %b, Actual: %b\n\n", expected, actual);
            return false;
        }

        return true;
    }

    private static boolean assertEquals(int expected, int actual) {
        if (expected != actual) {
            System.out.println("\tAssert Failed!");
            System.out.printf("\tExpected: %d, Actual: %d\n\n", expected, actual);
            return false;
        }

        return true;
    }
}
