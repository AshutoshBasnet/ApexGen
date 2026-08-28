package com.evolution.engine;

import com.evolution.model.Creature;
import com.evolution.model.Food;
import com.evolution.model.Genome;
import com.evolution.spatial.QuadTree;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Core Genetic Algorithm and Simulation Lifecycle Engine.
 *
 * Implements:
 * 1. O(N log N) QuadTree Spatial Partitioning for real-time physics.
 * 2. O(N log K) Elitism selection via Max-Heap PriorityQueue.
 * 3. O(log N) Fitness-Proportional Parent Selection via Prefix Sum array and Binary Search.
 */
public class EvolutionEngine {
    private final double arenaWidth;
    private final double arenaHeight;
    private final Random rng;

    // Simulation Configuration
    private int populationSize;
    private int initialFoodCount;
    private int maxTicksPerGeneration;
    private double elitismRatio;
    private double mutationRate;
    private double mutationScale;

    // Runtime State
    private int currentGeneration;
    private int currentTick;
    private boolean running;

    private final List<Creature> creatures;
    private final List<Food> foods;
    private final List<EvolutionStats> statsHistory;

    private QuadTree<Food> foodQuadTree;
    private QuadTree<Creature> creatureQuadTree;

    /**
     * Constructs the EvolutionEngine with standard biological defaults.
     *
     * @param arenaWidth  Width of the 2D world
     * @param arenaHeight Height of the 2D world
     */
    public EvolutionEngine(double arenaWidth, double arenaHeight) {
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.rng = new Random();

        this.populationSize = 75;
        this.initialFoodCount = 160;
        this.maxTicksPerGeneration = 300;
        this.elitismRatio = 0.05; // Top 5%
        this.mutationRate = 0.12;  // 12% per gene
        this.mutationScale = 8.0;

        this.creatures = new ArrayList<>(populationSize);
        this.foods = new ArrayList<>(initialFoodCount);
        this.statsHistory = new ArrayList<>();

        this.currentGeneration = 1;
        this.currentTick = 0;
        this.running = false;

        initializeSimulation();
    }

    /**
     * Initializes or resets the simulation to Generation 1 with randomized genomes.
     */
    /**
     * Generates clean spreadsheet-style alphabetical names: Apex-A, Apex-B ... Apex-Z, Apex-AA, Apex-AB...
     */
    public static String getAlphabeticalName(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index;
        while (n >= 0) {
            sb.insert(0, (char) ('A' + (n % 26)));
            n = (n / 26) - 1;
        }
        return "Apex-" + sb.toString();
    }

    /**
     * Initializes the simulation state with a fresh randomized population and food.
     */
    public synchronized void initializeSimulation() {
        creatures.clear();
        foods.clear();
        statsHistory.clear();
        currentGeneration = 1;
        currentTick = 0;

        // Initialize randomized population with alphabetical names
        for (int i = 0; i < populationSize; i++) {
            double x = 40.0 + rng.nextDouble() * (arenaWidth - 80.0);
            double y = 40.0 + rng.nextDouble() * (arenaHeight - 80.0);
            Genome genome = Genome.createRandom(rng);
            creatures.add(new Creature(getAlphabeticalName(i), x, y, genome, rng));
        }

        spawnFood(initialFoodCount);
        rebuildSpatialTrees();
    }

    /**
     * Spawns food uniformly throughout the arena bounds.
     */
    private void spawnFood(int count) {
        foods.clear();
        for (int i = 0; i < count; i++) {
            double x = 20.0 + rng.nextDouble() * (arenaWidth - 40.0);
            double y = 20.0 + rng.nextDouble() * (arenaHeight - 40.0);
            foods.add(new Food(x, y, 42.0));
        }
    }

    /**
     * Rebuilds both QuadTree spatial indexes from scratch for the current tick.
     */
    private void rebuildSpatialTrees() {
        Rectangle2D.Double bounds = new Rectangle2D.Double(0, 0, arenaWidth, arenaHeight);
        foodQuadTree = new QuadTree<>(bounds, 6);
        creatureQuadTree = new QuadTree<>(bounds, 6);

        for (Food food : foods) {
            if (!food.isEaten()) {
                foodQuadTree.insert(food);
            }
        }

        for (Creature creature : creatures) {
            if (creature.isAlive()) {
                creatureQuadTree.insert(creature);
            }
        }
    }

    /**
     * Executes a single simulation step / tick.
     * Updates all creatures, processes eating & deaths, and triggers generation turnover.
     */
    public synchronized void update() {
        currentTick++;

        // Rebuild QuadTree for fast spatial queries in O(N log N)
        rebuildSpatialTrees();

        int aliveCount = 0;
        for (Creature creature : creatures) {
            if (creature.isAlive()) {
                creature.update(foodQuadTree, arenaWidth, arenaHeight, rng);
                if (creature.isAlive()) {
                    aliveCount++;
                }
            }
        }

        // Clean up eaten foods
        foods.removeIf(Food::isEaten);

        // Generation turnover conditions: max ticks reached or all creatures dead
        if (currentTick >= maxTicksPerGeneration || aliveCount == 0) {
            evolveNextGeneration();
        }
    }

    /**
     * Executes the Genetic Algorithm lifecycle to produce the next generation:
     * 1. Record generation statistics and trait averages.
     * 2. DSA 1: Elitism via Max-Heap PriorityQueue (extract top 5% fittest).
     * 3. DSA 2: O(log N) Roulette Wheel Selection via Prefix Sum array + Binary Search.
     * 4. Perform Crossover and Gaussian Mutation.
     * 5. Respawn food and re-initialize creature positions.
     */
    public synchronized void evolveNextGeneration() {
        // 1. Compute & record Generation Statistics
        recordGenerationStats();

        List<Creature> nextGeneration = new ArrayList<>(populationSize);

        // --- DSA 1: ELITISM (Max-Heap PriorityQueue) ---
        // Max-Heap ordered descending by fitness: O(N log K)
        PriorityQueue<Creature> maxHeap = new PriorityQueue<>(
            Collections.reverseOrder()
        );
        maxHeap.addAll(creatures);

        int eliteCount = Math.max(1, (int) (populationSize * elitismRatio));
        for (int i = 0; i < eliteCount && !maxHeap.isEmpty(); i++) {
            Creature elite = maxHeap.poll();
            // Preserve the champion's genome directly without mutation
            Genome eliteGenome = new Genome(elite.getGenome());
            double x = 40.0 + rng.nextDouble() * (arenaWidth - 80.0);
            double y = 40.0 + rng.nextDouble() * (arenaHeight - 80.0);
            nextGeneration.add(new Creature(getAlphabeticalName(nextGeneration.size()), x, y, eliteGenome, rng));
        }

        // --- DSA 2: ROULETTE WHEEL SELECTION (Prefix Sums + Binary Search) ---
        // Build prefix sum array of fitness values in O(N)
        double[] prefixSums = new double[creatures.size()];
        prefixSums[0] = Math.max(0.001, creatures.get(0).getFitness());
        for (int i = 1; i < creatures.size(); i++) {
            prefixSums[i] = prefixSums[i - 1] + Math.max(0.001, creatures.get(i).getFitness());
        }
        double totalFitness = prefixSums[prefixSums.length - 1];

        // Produce remainder of population through reproduction
        while (nextGeneration.size() < populationSize) {
            Creature parentA = selectParentBinarySearch(prefixSums, totalFitness);
            Creature parentB = selectParentBinarySearch(prefixSums, totalFitness);

            // Crossover
            Genome childGenome = parentA.getGenome().crossover(parentB.getGenome(), rng);

            // Mutation
            childGenome.mutate(mutationRate, mutationScale, rng);

            double x = 40.0 + rng.nextDouble() * (arenaWidth - 80.0);
            double y = 40.0 + rng.nextDouble() * (arenaHeight - 80.0);
            nextGeneration.add(new Creature(getAlphabeticalName(nextGeneration.size()), x, y, childGenome, rng));
        }

        // Replace current population with next generation
        creatures.clear();
        creatures.addAll(nextGeneration);

        // Reset state for new generation
        currentGeneration++;
        currentTick = 0;
        spawnFood(initialFoodCount);
        rebuildSpatialTrees();
    }

    /**
     * Selects a parent creature in O(log N) time using a cumulative prefix sum array
     * and Java's Arrays.binarySearch().
     *
     * @param prefixSums   Cumulative fitness prefix sums
     * @param totalFitness Sum of all fitness values in the population
     * @return Selected Creature parent
     */
    private Creature selectParentBinarySearch(double[] prefixSums, double totalFitness) {
        double r = rng.nextDouble() * totalFitness;
        int idx = Arrays.binarySearch(prefixSums, r);

        if (idx < 0) {
            // Target falls between values; insertion point is -(idx + 1)
            idx = -(idx + 1);
        }

        // Clamp index bounds
        if (idx >= creatures.size()) {
            idx = creatures.size() - 1;
        }

        return creatures.get(idx);
    }

    /**
     * Aggregates population trait averages and metrics, recording them into statsHistory.
     */
    private void recordGenerationStats() {
        double totalSpeed = 0;
        double totalSize = 0;
        double totalStrength = 0;
        double totalFitness = 0;
        double maxFitness = 0;
        int survivors = 0;

        for (Creature c : creatures) {
            Genome g = c.getGenome();
            totalSpeed += g.getSpeed();
            totalSize += g.getSize();
            totalStrength += g.getStrength();

            double fit = c.getFitness();
            totalFitness += fit;
            if (fit > maxFitness) {
                maxFitness = fit;
            }

            if (c.isAlive()) {
                survivors++;
            }
        }

        int n = creatures.size();
        EvolutionStats stats = new EvolutionStats(
            currentGeneration,
            n > 0 ? totalSpeed / n : 0,
            n > 0 ? totalSize / n : 0,
            n > 0 ? totalStrength / n : 0,
            n > 0 ? totalFitness / n : 0,
            maxFitness,
            survivors,
            n
        );

        statsHistory.add(stats);
    }

    // Getters & Setters
    public synchronized List<Creature> getCreatures() {
        return creatures;
    }

    public synchronized List<Food> getFoods() {
        return foods;
    }

    public synchronized QuadTree<Food> getFoodQuadTree() {
        return foodQuadTree;
    }

    public synchronized QuadTree<Creature> getCreatureQuadTree() {
        return creatureQuadTree;
    }

    public synchronized List<EvolutionStats> getStatsHistory() {
        return statsHistory;
    }

    public synchronized int getCurrentGeneration() {
        return currentGeneration;
    }

    public synchronized int getCurrentTick() {
        return currentTick;
    }

    public int getMaxTicksPerGeneration() {
        return maxTicksPerGeneration;
    }

    public synchronized int getAliveCount() {
        int count = 0;
        for (Creature c : creatures) {
            if (c.isAlive()) count++;
        }
        return count;
    }

    public synchronized int getFoodCount() {
        return foods.size();
    }

    public synchronized double getAvgSpeed() {
        if (creatures.isEmpty()) return 0;
        double sum = 0;
        for (Creature c : creatures) sum += c.getGenome().getSpeed();
        return sum / creatures.size();
    }

    public synchronized double getAvgSize() {
        if (creatures.isEmpty()) return 0;
        double sum = 0;
        for (Creature c : creatures) sum += c.getGenome().getSize();
        return sum / creatures.size();
    }

    public synchronized double getAvgStrength() {
        if (creatures.isEmpty()) return 0;
        double sum = 0;
        for (Creature c : creatures) sum += c.getGenome().getStrength();
        return sum / creatures.size();
    }

    public synchronized double getAvgEnergy() {
        if (creatures.isEmpty()) return 0;
        double sum = 0;
        int alive = 0;
        for (Creature c : creatures) {
            if (c.isAlive()) {
                sum += c.getEnergy();
                alive++;
            }
        }
        return alive > 0 ? sum / alive : 0;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public void setInitialFoodCount(int initialFoodCount) {
        this.initialFoodCount = initialFoodCount;
    }

    public void setMaxTicksPerGeneration(int maxTicksPerGeneration) {
        this.maxTicksPerGeneration = maxTicksPerGeneration;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public void setMutationScale(double mutationScale) {
        this.mutationScale = mutationScale;
    }

    public double getArenaWidth() {
        return arenaWidth;
    }

    public double getArenaHeight() {
        return arenaHeight;
    }

    public double getElitismRatio() {
        return elitismRatio;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public double getMutationScale() {
        return mutationScale;
    }

    /**
     * Extracts the top K creatures by fitness from the current generation using a Max-Heap PriorityQueue in O(N log K).
     */
    public synchronized List<Creature> getTopCreatures(int k) {
        PriorityQueue<Creature> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        maxHeap.addAll(creatures);
        List<Creature> topList = new ArrayList<>();
        int count = Math.min(k, maxHeap.size());
        for (int i = 0; i < count; i++) {
            topList.add(maxHeap.poll());
        }
        return topList;
    }
}
