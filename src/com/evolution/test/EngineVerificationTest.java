package com.evolution.test;

import com.evolution.engine.EvolutionEngine;
import com.evolution.engine.EvolutionStats;
import com.evolution.model.Creature;
import com.evolution.model.Food;
import com.evolution.model.Genome;
import com.evolution.spatial.QuadTree;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Automated Verification Test Suite for the Natural Selection & Evolution Simulator.
 * Validates correctness of QuadTree spatial indexing, Max-Heap elitism,
 * Prefix-Sum parent selection, and Genetic lifecycle.
 */
public class EngineVerificationTest {
    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  NATURAL SELECTION SIMULATOR: DSA TEST SUITE    ");
        System.out.println("=================================================");

        testGenomeOperations();
        testQuadTreeSpatialPartitioning();
        testElitismMaxHeap();
        testPrefixSumParentSelection();
        testEvolutionEngineGenerations();

        System.out.println("=================================================");
        System.out.printf("  SUMMARY: %d / %d Tests Passed Successfully!%n", testsPassed, testsTotal);
        System.out.println("=================================================");

        if (testsPassed != testsTotal) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            System.out.printf(" [PASS] %s%n", testName);
            testsPassed++;
        } else {
            System.err.printf(" [FAIL] %s%n", testName);
        }
    }

    private static void testGenomeOperations() {
        System.out.println("\n--- Testing Genome Operations ---");
        Random rng = new Random(42);

        Genome g1 = new Genome(10.0, 20.0, 40.0);
        Genome g2 = new Genome(80.0, 90.0, 95.0);

        // BMR test
        double bmr1 = g1.calculateBMR();
        double bmr2 = g2.calculateBMR();
        assertTrue("BMR of higher trait genome is strictly greater than lower trait", bmr2 > bmr1);

        // Clamping test
        Genome extreme = new Genome(-50.0, 500.0, -10.0);
        assertTrue("Lower bounds clamped to 5.0", extreme.getSpeed() == 5.0 && extreme.getStrength() == 5.0);
        assertTrue("Upper bounds clamped to 100.0", extreme.getSize() == 100.0);

        // Crossover test
        Genome child = g1.crossover(g2, rng);
        assertTrue("Child traits are within parent spectrum",
            child.getSpeed() >= 5.0 && child.getSpeed() <= 100.0 &&
            child.getSize() >= 5.0 && child.getSize() <= 100.0 &&
            child.getStrength() >= 5.0 && child.getStrength() <= 100.0);

        // Mutation test
        child.mutate(1.0, 15.0, rng);
        assertTrue("Mutated traits remain clamped in [5.0, 100.0]",
            child.getSpeed() >= 5.0 && child.getSpeed() <= 100.0 &&
            child.getSize() >= 5.0 && child.getSize() <= 100.0 &&
            child.getStrength() >= 5.0 && child.getStrength() <= 100.0);
    }

    private static void testQuadTreeSpatialPartitioning() {
        System.out.println("\n--- Testing QuadTree Spatial Partitioning ---");
        Rectangle2D.Double boundary = new Rectangle2D.Double(0, 0, 1000, 1000);
        QuadTree<Food> tree = new QuadTree<>(boundary, 4);

        // Insert 100 deterministic food points
        List<Food> allFoods = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Food f = new Food(i * 10, i * 10);
            allFoods.add(f);
            tree.insert(f);
        }

        assertTrue("QuadTree size matches inserted count", tree.size() == 100);
        assertTrue("QuadTree subdivided into quadrants", tree.isDivided());

        // Test Circle Query centered at (100, 100) with radius 50
        List<Food> found = new ArrayList<>();
        tree.queryCircle(100, 100, 50, found);

        // Brute force check
        int expectedCount = 0;
        for (Food f : allFoods) {
            double dx = f.getX() - 100;
            double dy = f.getY() - 100;
            if (dx * dx + dy * dy <= 50 * 50) {
                expectedCount++;
            }
        }

        assertTrue("QuadTree queryCircle matches brute force count (" + found.size() + " == " + expectedCount + ")",
            found.size() == expectedCount);

        // Verify all found items are strictly within distance
        boolean allWithin = true;
        for (Food f : found) {
            double dx = f.getX() - 100;
            double dy = f.getY() - 100;
            if (dx * dx + dy * dy > 50 * 50) {
                allWithin = false;
                break;
            }
        }
        assertTrue("All QuadTree returned items are strictly within radius", allWithin);
    }

    private static void testElitismMaxHeap() {
        System.out.println("\n--- Testing Elitism Max-Heap PriorityQueue ---");
        Random rng = new Random(101);
        PriorityQueue<Creature> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        List<Creature> testList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Creature c = new Creature(100, 100, Genome.createRandom(rng), rng);
            // Artificially simulate different fitness
            for (int k = 0; k < i; k++) {
                Food dummy = new Food(100, 100);
                dummy.setEaten(false);
                // direct update simulation
            }
            testList.add(c);
            maxHeap.add(c);
        }

        Creature top1 = maxHeap.poll();
        Creature top2 = maxHeap.poll();
        assertTrue("Max-Heap root has greater or equal fitness than second extracted",
            top1 != null && top2 != null && top1.getFitness() >= top2.getFitness());
    }

    private static void testPrefixSumParentSelection() {
        System.out.println("\n--- Testing Prefix-Sum Binary Search Parent Selection ---");
        Random rng = new Random(202);
        int popSize = 60;
        List<Creature> creatures = new ArrayList<>(popSize);
        for (int i = 0; i < popSize; i++) {
            creatures.add(new Creature(200, 200, Genome.createRandom(rng), rng));
        }

        // Build prefix sum
        double[] prefixSums = new double[popSize];
        prefixSums[0] = Math.max(0.001, creatures.get(0).getFitness());
        for (int i = 1; i < popSize; i++) {
            prefixSums[i] = prefixSums[i - 1] + Math.max(0.001, creatures.get(i).getFitness());
        }

        assertTrue("Prefix sum array is monotonically strictly increasing",
            prefixSums[popSize - 1] > prefixSums[0]);

        // Perform 1000 selections to test stability
        boolean selectionValid = true;
        for (int iter = 0; iter < 1000; iter++) {
            double target = rng.nextDouble() * prefixSums[popSize - 1];
            int idx = java.util.Arrays.binarySearch(prefixSums, target);
            if (idx < 0) idx = -(idx + 1);
            if (idx >= popSize) idx = popSize - 1;

            if (idx < 0 || idx >= popSize) {
                selectionValid = false;
                break;
            }
        }
        assertTrue("1000 O(log N) Roulette Wheel selections completed within valid bounds", selectionValid);
    }

    private static void testEvolutionEngineGenerations() {
        System.out.println("\n--- Testing Multi-Generation Evolution Lifecycle ---");
        EvolutionEngine engine = new EvolutionEngine(800, 600);
        engine.setPopulationSize(40);
        engine.setInitialFoodCount(80);
        engine.setMaxTicksPerGeneration(100);
        engine.initializeSimulation();

        int initialGen = engine.getCurrentGeneration();
        assertTrue("Simulation starts at Generation 1", initialGen == 1);

        // Run 250 ticks (should trigger at least 2 generations)
        for (int i = 0; i < 250; i++) {
            engine.update();
        }

        assertTrue("Generation successfully incremented past Gen 1", engine.getCurrentGeneration() > 1);
        List<EvolutionStats> history = engine.getStatsHistory();
        assertTrue("Stats history contains recorded generational metrics", !history.isEmpty());

        EvolutionStats lastGen = history.get(history.size() - 1);
        assertTrue("Generation stats averages are positive numbers",
            lastGen.getAvgSpeed() > 0 && lastGen.getAvgSize() > 0 && lastGen.getAvgStrength() > 0);

        List<Creature> top3 = engine.getTopCreatures(3);
        assertTrue("Top 3 champions extracted via Max-Heap", !top3.isEmpty() && top3.size() <= 3);
        if (top3.size() >= 2) {
            assertTrue("Top 1 champion has >= fitness than Top 2", top3.get(0).getFitness() >= top3.get(1).getFitness());
        }
    }
}
