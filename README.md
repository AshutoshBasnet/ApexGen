# 🧬 ApexGen: Natural Selection & Genetic Drift Simulator

<div align="center">

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=github-actions&logoColor=white)
![Dependencies](https://img.shields.io/badge/Dependencies-Zero%20(Pure%20Java)-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)
![DSA](https://img.shields.io/badge/Focus-Data%20Structures%20%26%20Algorithms-orange?style=for-the-badge)

**A high-performance biological evolution and natural selection simulation engine powered by custom Data Structures & Algorithms.**

[Overview](#-overview) •
[Core DSA Implementations](#-core-data-structures--algorithms) •
[Ecosystem Mechanics](#-ecosystem-mechanics--genetics) •
[HUD & Analytics](#-hud--analytics-dashboard) •
[Getting Started](#-getting-started) •
[Architecture](#-project-architecture)

</div>

---

## 🌟 Overview

**ApexGen** is an autonomous multi-agent evolution simulator engineered in **Pure Java** (Swing/AWT) with **zero third-party dependencies**. 

In ApexGen, a randomized population of creatures navigates a dynamic environment with finite resources. Survival and reproduction are governed by genetic traits, metabolic energy trade-offs, spatial proximity, and natural selection. Over generations, the population exhibits emergent behaviors, speciation, and evolutionary drift.

The project is built specifically to demonstrate real-world, high-performance implementations of fundamental **Data Structures and Algorithms (DSA)** to solve computational bottlenecks at 60 FPS.

---

## ⚡ Core Data Structures & Algorithms

ApexGen is architected around three critical DSA pillars that turn what would be an $O(N^2)$ simulation into a streamlined, high-FPS real-time engine:

```
                                  APEXGEN DSA PIPELINE
  ┌──────────────────────────────────────────────────────────────────────────────────┐
  │                                                                                  │
  │  [ Spatial Neighborhood ]       [ Elitism Selection ]     [ Parent Selection ]   │
  │                                                                                  │
  │     2D QuadTree Partitioning         Max-Heap PriorityQueue    Prefix-Sum + Binary Search │
  │     O(log M) Neighborhood Query      O(N log K) Top Elites     O(log N) Roulette Sampling │
  │                                                                                  │
  └──────────────────────────────────────────────────────────────────────────────────┘
```

### 1. 🌲 2D Spatial Partitioning: QuadTree (`com.evolution.spatial.QuadTree`)
* **The Problem**: In an arena with $N$ creatures and $M$ food items, brute-force proximity detection requires $N \times M$ Euclidean distance calculations every single frame. At 60 FPS, this causes severe performance bottlenecks.
* **DSA Solution**: A custom generic `QuadTree<T extends PointItem>` recursively subdivides 2D space into four quadrants (`NW`, `NE`, `SW`, `SE`) when capacity exceeds bucket threshold $K$.
* **Pruning**: Circular range queries (`queryCircle`) immediately prune non-intersecting quadrants.
* **Complexity**:
  * **Construction / Rebuild**: $\mathcal{O}(M \log M)$
  * **Range Query**: $\mathcal{O}(\log M)$ average case

---

### 2. 👑 Elite Lineage Preservation: Max-Heap (`java.util.PriorityQueue<Creature>`)
* **The Problem**: Random crossover and mutation can inadvertently degrade top-tier genetic champions across generations.
* **DSA Solution**: At the end of each generation, the population is loaded into a `PriorityQueue<Creature>` configured with a Max-Heap comparator indexed on individual fitness.
* **Complexity**:
  * **Top-$K$ Extraction**: $\mathcal{O}(N \log K)$ to preserve exact copies of the top 5% fittest lineages into the subsequent generation.

---

### 3. 🎯 Fast Roulette Wheel Selection: 1D Prefix Sums + Binary Search
* **The Problem**: Sampling parent mates proportional to fitness via linear scanning is an $\mathcal{O}(N)$ bottleneck across hundreds of mating events.
* **DSA Solution**:
  1. Construct a cumulative 1D Prefix Sum array $P[i] = P[i-1] + \text{fitness}[i]$ in $\mathcal{O}(N)$.
  2. Sample a random uniform double $r \in [0, P[N-1])$.
  3. Execute `Arrays.binarySearch(P, r)` to identify parent index in $\mathcal{O}(\log N)$ time.
* **Complexity**:
  * **Selection**: $\mathcal{O}(\log N)$ per parent mate.

---

### 📊 Algorithmic Complexity Matrix

| Operation | Naive Approach | **ApexGen DSA Approach** | Theoretical Speedup |
| :--- | :---: | :---: | :---: |
| **Spatial Foraging Query** | $\mathcal{O}(N \cdot M)$ | $\mathcal{O}(N \log M)$ (QuadTree) | **Exponential** |
| **Elite Champion Selection** | $\mathcal{O}(N \log N)$ (Full Sort) | $\mathcal{O}(N \log K)$ (Max-Heap) | **Significant ($K \ll N$)** |
| **Parent Roulette Selection** | $\mathcal{O}(N)$ | $\mathcal{O}(\log N)$ (Prefix Sum + Binary Search) | **Logarithmic** |
| **Generational Stats Aggregation**| $\mathcal{O}(N)$ | $\mathcal{O}(N)$ (Single-pass reduction) | **Optimal** |

---

## 🧬 Ecosystem Mechanics & Genetics

Each creature's morphology and behavior are encoded within a 3-gene `Genome`:

```
               ┌─────────────── GENOME TRAIT VECTOR ───────────────┐
               │                                                    │
               │   • Speed    [5.0 - 100.0]  → Movement Velocity    │
               │   • Size     [5.0 - 100.0]  → Mass & Fat Storage   │
               │   • Strength [5.0 - 100.0]  → Digestion Efficiency │
               │                                                    │
               └────────────────────────────────────────────────────┘
```

### 1. ⚙️ Basal Metabolic Rate (BMR)
Movement, body mass, and muscular capacity incur continuous metabolic energy costs calculated per tick:
$$\text{BMR} = 0.12 + 0.0035 \cdot \text{speed}^{1.4} + 0.0025 \cdot \text{size}^{1.6} + 0.0018 \cdot \text{strength}$$

* **Evolutionary Trade-off**: High speed and large size allow rapid foraging and high maximum energy storage, but dramatically increase energy decay, leading to starvation if food is scarce.

### 2. 🔀 Recombination & Mutation
* **Uniform & Two-Point Crossover**: Combines maternal and paternal gene segments.
* **Gaussian Mutation**: Perturbs genes ($\mu = 0, \sigma = 8.0$) within hard-clamped bounds $[5.0, 100.0]$.

### 3. 🏆 Fitness Function
$$\text{Fitness} = (\text{Food Eaten} \times 160.0) + (\text{Survival Ticks} \times 0.35) + \text{Energy Bonus}$$

---

## 🖥️ HUD & Analytics Dashboard

<div align="center">

| Component | Description |
| :--- | :--- |
| **Simulation Viewport** | 60 FPS double-buffered arena displaying creatures with dynamic trait coloration (Red = Strength, Blue = Speed, Green = Size mix), heading vectors, and mini energy bars. |
| **Interactive Inspector** | Click any creature to inspect real-time fitness, energy reserves, food eaten, age, and individual genetic traits. |
| **Population Trait Meters** | Live gauge meters monitoring mean population Speed, Size, and Strength. |
| **Drift Line Graph** | Real-time multi-series chart rendering evolutionary drift across generations. |
| **Simulation Controls** | Play/Pause, Step 1x single-tick debugging, Speed Multiplier (1x to 10x), and QuadTree Grid overlay toggles. |

</div>

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK 17 or higher)**
* Windows, macOS, or Linux
* No build tools (Maven/Gradle) or external libraries required.

### 🔨 Compilation
Clone the repository and compile using `build.bat` (Windows) or standard `javac`:

```bash
# Windows
build.bat

# Linux / macOS
mkdir -p bin
javac -d bin -sourcepath src src/com/evolution/model/*.java src/com/evolution/spatial/*.java src/com/evolution/engine/*.java src/com/evolution/ui/*.java src/com/evolution/test/*.java
```

### 🧪 Running Verification Tests
Execute the standalone automated verification suite validating QuadTree partitioning, Max-Heap elitism, Roulette selection, and Genome operations:

```bash
java -cp bin com.evolution.test.EngineVerificationTest
```

### ▶️ Launching the Application
Run `run.bat` or launch the GUI directly:

```bash
# Windows
run.bat

# Linux / macOS
java -cp bin com.evolution.ui.MainGUI
```

---

## 📂 Project Architecture

```
ApexGen/
├── src/
│   └── com/
│       └── evolution/
│           ├── model/
│           │   ├── PointItem.java            # Spatial 2D coordinate interface
│           │   ├── Genome.java               # Genetic traits, non-linear BMR, crossover, mutation
│           │   ├── Food.java                 # Nutritional entity implementing PointItem
│           │   └── Creature.java             # Autonomous agent with physics, energy, and kinematics
│           ├── spatial/
│           │   └── QuadTree.java             # Generic 2D QuadTree with circular neighborhood queries
│           ├── engine/
│           │   ├── EvolutionEngine.java      # Main lifecycle loop, Max-Heap & Binary Search selection
│           │   └── EvolutionStats.java       # Generational metrics snapshot
│           ├── ui/
│           │   ├── SimulationPanel.java      # Double-buffered 60 FPS viewport & click inspector
│           │   ├── AnalyticsPanel.java       # HUD metrics, trait meters, real-time drift chart
│           │   └── MainGUI.java              # Swing frame window & DSA educational modal dialogs
│           └── test/
│               └── EngineVerificationTest.java # Comprehensive automated test suite
├── bin/                                      # Compiled bytecode binaries
├── build.bat                                 # Windows build automation script
├── run.bat                                   # Windows launch automation script
└── README.md                                 # Project documentation
```

---

## 📜 License

This project is licensed under the **MIT License** — feel free to use, modify, and build upon it for academic and personal projects.

<div align="center">
Developed with ❤️ in Pure Java.
</div>
