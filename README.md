# AI Optimal Pathfinder

An autonomous, graph-based pathfinding engine and real-time simulation in Java. The system converts procedurally generated 2D grid environments into weighted directed graphs (DAGs), evaluates optimal trajectories using multiple shortest-path algorithms, and deploys an autonomous tracer agent that navigates the environment in real time.

---

## Key Features

- **Procedural Grid Generation**: Generates 2D tile maps ($40 \times 15$) with randomized intersections, orthogonal road corridors, obstacle boundaries, and probabilistic reward/penalty item placements.
- **Dynamic Graph Construction**: Scans the grid environment and abstracts road networks into an Adjacency List directed graph, aggregating cumulative edge weights along corridors between intersections.
- **Multi-Algorithm Optimization Engine**:
  - **Unweighted BFS**: Minimal intersection traversal for unweighted grids.
  - **Kahn's Topological Sort + DAG Relaxation**: Linear-time optimal pathfinding for directed acyclic graphs.
  - **Dijkstra's Algorithm**: Non-negative weighted shortest path optimization.
  - **Bellman-Ford Algorithm**: General-weight shortest path optimization supporting negative edge costs and negative cycle detection.
- **Real-Time Autonomous Agent**: The tracer agent tracks its heading (`UP`, `DOWN`, `LEFT`, `RIGHT`, `STOP`), validates intersection transitions, and renders real-time breadcrumbs (`.`) along the active path.
- **Built-in Verification Suite**: Comprehensive unit tests validating graph building, edge weighting, topological sorting, shortest path solvers, and negative cycle handling.

---

## Pathfinding Algorithms

The engine features four distinct routing algorithms selectable via the mode menu:

| Mode | Algorithm | Time Complexity | Space Complexity | Supported Graph Types | Weight Handling |
| :---: | :--- | :---: | :---: | :--- | :--- |
| **`0`** | **Breadth-First Search (BFS)** | $\mathcal{O}(V + E)$ | $\mathcal{O}(V)$ | Unweighted Graphs / Plain Maps | Ignores weights; minimizes intersection count |
| **`1`** | **DAG Shortest Path (Topological Sort)** | $\mathcal{O}(V + E)$ | $\mathcal{O}(V)$ | Directed Acyclic Graphs (DAGs) | Linear-time relaxation using Kahn's algorithm |
| **`2`** | **Dijkstra's Algorithm** | $\mathcal{O}(V^2)$ / $\mathcal{O}(E \log V)$ | $\mathcal{O}(V)$ | Directed Graphs with non-negative weights | Minimizes cumulative penalty costs |
| **`3`** | **Bellman-Ford Algorithm** | $\mathcal{O}(V \times E)$ | $\mathcal{O}(V)$ | General Directed Graphs | Supports mixed rewards/penalties & detects negative cycles |

### Algorithmic Mechanics

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
| `P` | **Tracer / Agent** | The active autonomous agent traversing the grid. |
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

When you launch `ModelCode_AutoTracer`, the simulation prompts you in the console to select a configuration mode:

```text
Select Test Setup:
0 - Plain Map, RIGHT/DOWN Direction Only (DAG)
1 - Map with Random Penalties (DAG): Topological Sort Shortest Path Algorithm
2 - Map with Random Penalties (DAG): Dijkstra Shortest Path Algorithm
3 - Map with Random Penalties and Rewards (DAG): Bellman-Ford Shortest Path Algorithm
```

### Running the Simulation
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