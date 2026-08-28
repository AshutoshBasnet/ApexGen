# 🧬 ApexGen: Natural Selection & Genetic Drift Simulator

<div align="center">

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=github-actions&logoColor=white)
![Dependencies](https://img.shields.io/badge/Dependencies-Zero%20(Pure%20Java)-blue?style=for-the-badge)
![DSA](https://img.shields.io/badge/Focus-Data%20Structures%20%26%20Algorithms-orange?style=for-the-badge)

**A high-performance biological evolution and natural selection simulation engine powered by custom Data Structures & Algorithms.**

[Overview](#-overview) •
[Data Structures & Algorithms Used](#-data-structures--algorithms-used) •
[Ecosystem & Genetics](#-ecosystem--genetics) •
[HUD & Features](#-hud--features) •
[Getting Started](#-getting-started) •
[Project Structure](#-project-structure)

</div>

---

## 🌟 Overview

**ApexGen** is an interactive artificial life simulator built in **100% pure standard Java** (Swing/AWT) with **zero third-party dependencies**.

In ApexGen, a randomized population of autonomous creatures lives in an environment with limited food. Each creature's survival depends on its genetic traits (**Speed**, **Size**, and **Strength**). High speed and large size allow creatures to find food quickly, but consume more energy (metabolism). Those that collect enough food survive and pass down their traits to the next generation, while others starve.

Over multiple generations, you can watch natural selection and genetic drift emerge in real time on the live analytics graph.

---

## ⚡ Data Structures & Algorithms Used

ApexGen uses three core Data Structures & Algorithms to keep the simulation fast and responsive at 60 FPS:

```
                                APEXGEN DSA WORKFLOW
  ┌─────────────────────────────────────────────────────────────────────────────┐
  │                                                                             │
  │   1. Finding Nearby Food         2. Top Champions        3. Choosing Parents│
  │                                                                             │
  │        2D QuadTree               Max-Heap (PriorityQueue)  Prefix Sum Array │
  │    (Spatial Partitioning)        (Elitism & Leaderboard)   + Binary Search  │
  │                                                                             │
  └─────────────────────────────────────────────────────────────────────────────┘
```

### 1. 🌲 2D QuadTree (Spatial Partitioning)
* **What it does**: Helps creatures find nearby food instantly without lagging.
* **How it works**:
  * Instead of every creature checking the distance to every single food item across the entire arena (which becomes very slow), the arena is divided into 4 quadrants (`NW`, `NE`, `SW`, `SE`).
  * If a quadrant gets too crowded, it recursively subdivides into 4 smaller sub-quadrants.
  * When a creature looks for food, it only searches its immediate quadrant box, instantly ignoring 75%+ of the map.

---

### 2. 👑 Max-Heap PriorityQueue (Elitism & Top 3 Champions)
* **What it does**: Identifies the fittest creatures in real time and protects the best genes from being lost.
* **How it works**:
  * All creatures are tracked in a Max-Heap (`PriorityQueue<Creature>`) ordered by fitness score.
  * The root of the heap always holds the highest fitness creature.
  * **Top 3 Leaderboard**: The HUD queries the heap to display the Top 3 champions with live fitness and Alive/Dead status.
  * **Elitism**: The top 5% fittest champions are copied directly into the next generation without mutation to preserve champion bloodlines.

---

### 3. 🎯 Prefix Sum Array + Binary Search (Roulette Wheel Parent Selection)
* **What it does**: Chooses parent mates fairly and quickly so that fitter creatures have a higher chance of reproducing.
* **How it works**:
  1. Creates a running cumulative total array (Prefix Sums) of all creatures' fitness scores.
  2. Picks a random number between `0` and total fitness.
  3. Uses **Binary Search** (`Arrays.binarySearch`) to find the chosen parent in logarithmic time instead of scanning through the entire list one by one.

---

## 🧬 Ecosystem & Genetics

Each creature has a 3-gene `Genome`:
* **Speed** (`5.0 - 100.0`): How fast the creature moves across the arena.
* **Size** (`5.0 - 100.0`): Physical radius and maximum energy storage capacity.
* **Strength** (`5.0 - 100.0`): Nutritional efficiency when consuming food.

### ⚖️ Energy & Metabolism Trade-offs
* Every movement costs energy (Basal Metabolic Rate).
* Faster and bigger creatures burn energy much faster. If they don't eat in time, they run out of energy and die.

### 🔀 Crossover & Mutation
* Offspring inherit a mix of genes from two selected parents (crossover).
* Small random mutations (+/- variations) are applied to introduce new diversity into the gene pool.

---

## 🖥️ HUD & Features

* **Zero Screen Clutter**: Creatures swim around cleanly with no text distraction.
* **Click-to-Inspect**: Clicking any creature shows a glowing golden ring, its name tag (e.g. `Apex-A`, `Apex-B`...), and a detailed tooltip with its fitness, age in years, food eaten, energy, and traits.
* **Top 3 Champions Leaderboard**: Displays the top 3 highest-fitness creatures with live fitness scores and **`Alive`** / **`Dead`** status badges. Clicking any champion locks the camera highlight onto them.
* **Epoch Progress (Year 0 to 300)**: Each generation lives through a 300-year epoch.
* **Live Evolutionary Drift Chart**: Real-time line graph tracking the average Speed, Size, and Strength of the population across generations.
* **Interactive Controls**: Pause/Play, Step 1x forward, Restart, and Speed Multiplier slider (1x to 10x).

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK 17 or higher)**
* Windows, macOS, or Linux
* Zero external libraries or build tools needed.

### 🔨 Compile and Run (Windows)
Double-click or run from command line:
```cmd
build.bat
run.bat
```

### 🔨 Compile and Run (Linux / macOS)
```bash
mkdir -p bin
javac -encoding UTF-8 -d bin -sourcepath src src/com/evolution/model/*.java src/com/evolution/spatial/*.java src/com/evolution/engine/*.java src/com/evolution/ui/*.java src/com/evolution/test/*.java
java -cp bin com.evolution.ui.MainGUI
```

### 🧪 Run Automated Tests
```bash
java -cp bin com.evolution.test.EngineVerificationTest
```

---

## 📂 Project Structure

```
ApexGen/
├── src/
│   └── com/
│       └── evolution/
│           ├── model/
│           │   ├── PointItem.java            # 2D coordinate item interface
│           │   ├── Genome.java               # Genetic traits, crossover, and mutation
│           │   ├── Food.java                 # Food entity
│           │   └── Creature.java             # Autonomous agent physics, energy & naming
│           ├── spatial/
│           │   └── QuadTree.java             # Generic 2D QuadTree spatial index
│           ├── engine/
│           │   ├── EvolutionEngine.java      # Main lifecycle loop, Max-Heap & Binary Search selection
│           │   └── EvolutionStats.java       # Generational metrics recorder
│           ├── ui/
│           │   ├── SimulationPanel.java      # 60 FPS arena canvas & click inspector
│           │   ├── AnalyticsPanel.java       # Top 3 leaderboard, trait meters, drift line chart
│           │   └── MainGUI.java              # Main window & control bar
│           └── test/
│               └── EngineVerificationTest.java # Automated DSA verification test suite
├── bin/                                      # Compiled class files
├── build.bat                                 # Windows compilation script
├── run.bat                                   # Windows launch script
└── README.md                                 # Project documentation
```
