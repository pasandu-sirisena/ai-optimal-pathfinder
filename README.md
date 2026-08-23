# AI Optimal Pathfinder

An autonomous, graph-based pathfinding engine and real-time simulation in Java. The system converts procedurally generated 2D grid environments into weighted directed graphs (DAGs), evaluates optimal trajectories using multiple shortest-path algorithms, and deploys an autonomous player that navigates the environment in real time.

---

## Pathfinding Algorithms

The engine features four distinct routing algorithms:

1. **Unweighted BFS (`UnweightedShortestPath`)**:
   Explores neighboring intersections level-by-level using a FIFO queue. Guarantees the shortest path in terms of total edges traversed.
2. **DAG Shortest Path (`DAGShortestPath` & `topologicalSort`)**:
   Computes in-degrees for all vertices and applies Kahn's algorithm to generate a topological ordering. Relaxes outgoing edges in topological sequence, achieving optimal linear $\mathcal{O}(V + E)$ performance.
3. **Dijkstra's Shortest Path (`DijkstraShortestPath`)**:
   Initializes distance maps to infinity, extracts the minimum-cost unvisited vertex greedily, and relaxes adjacent edges. Designed for maps containing only positive penalty weights.
4. **Bellman-Ford Shortest Path (`BellmanShortestPath`)**:
   Relaxes all edges $|V| - 1$ times across the graph. Performs a $|V|$-th iteration to detect negative weight cycles; returns `null` with a warning if an unbounded negative cycle is present.

---

## Simulation Rules

### Grid Layout
- **Dimensions**: $40$ columns ($\text{X}$) $\times$ $15$ rows ($\text{Y}$).
- **Start Node (`S`)**: Located at fixed coordinate `(1, 1)` (top-left).
- **Goal / Destination Node (`D`)**: Located at fixed coordinate `(38, 13)` (bottom-right).
- **Intersections (`I`)**: Procedurally placed road junctions where routing decisions occur.
- **Directionality**: Roads are directed **East (Right)** and **South (Down)**, ensuring a Directed Acyclic Graph (DAG) topology across the grid.

### Edge Weighting & Objective Function
Corridors connecting intersections can contain pickup items that alter the cost of traversing that road segment:
- **Score Penalty (`x`)**: Adds **$+5$** weight per pickup (representing cost/risk).
- **Score Reward (`$`)**: Adds **$-2$** weight per pickup (representing a score bonus / cost reduction).

$$\text{Edge Weight } (W) = \sum \text{Penalties} - \sum \text{Rewards} = (5 \times n_x) - (2 \times n_{\$})$$

The simulation's objective is to **maximize the net score** ($\text{Total Reward} - \text{Total Penalty}$), which directly corresponds to **minimizing the cumulative edge weight** along the path:

$$\min \sum_{e \in \text{Path}} W_e \iff \max \left( \sum \text{Rewards} - \sum \text{Penalties} \right)$$

---

## Legend

When the simulation runs in your terminal, the environment is rendered using ASCII character representations:

| Symbol | Meaning | Description |
| :---: | :--- | :--- |
| `P` | **Player** | The active player traversing the grid. |
| `S` | **Start Point** | Starting intersection at coordinate `(1, 1)`. |
| `D` | **Destination** | Goal intersection at coordinate `(38, 13)`. |
| `I` | **Intersection** | Road junctions where graph nodes are formed and decisions are made. |
| `#` | **Wall / Obstacle** | Impassable terrain bounding the road networks. |
| ` ` | **Road / Pathway** | Navigable open road. |
| `.` | **Optimal Route Path** | Breadcrumb markers indicating the computed optimal trajectory. |
| `x` | **Score Penalty** | Hazard pickup (+5 cost penalty). |
| `$` | **Score Reward** | Bonus pickup (-2 cost discount / reward). |

---

## Controls & Mode Selection

1. Enter `0`, `1`, `2`, or `3` and press `Enter`.
2. The simulation will procedurally generate the map and compute the optimal route using the selected algorithm.
3. The terminal will animate the tracer `P` following the route `.` from `S` to `D` in real time (refreshing every 100ms).
4. The console continuously prints the evaluated optimal path coordinate chain:
   ```text
   Optimal Path: 
   T(1, 1) => T(12, 1) => T(12, 8) => T(27, 8) => T(27, 13) => T(38, 13) => 
   ```
5. Once `P` reaches destination `D`, the simulation completes.

---

## How to Run

### Compilation

Clone the repository and compile all source files from the project root:

```bash
# Compile all Java source files
javac src/*.java
```

### Running the Simulation

Execute the main simulation class:

```bash
# Run from the project root using the classpath flag:
java -cp src ModelCode_AutoTracer

# Or change into the src directory and run directly:
cd src
java ModelCode_AutoTracer
```