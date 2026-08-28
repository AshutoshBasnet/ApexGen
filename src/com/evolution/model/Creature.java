package com.evolution.model;

import com.evolution.spatial.QuadTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents an autonomous agent in the evolutionary simulation.
 * Implements PointItem for QuadTree queries and Comparable for fitness sorting.
 */
public class Creature implements PointItem, Comparable<Creature> {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private double wanderAngle;

    private final String name;
    private final Genome genome;
    private double energy;
    private final double maxEnergy;
    private int foodEaten;
    private int survivalTicks;
    private boolean alive;

    // Derived physical properties based on Genome
    private final double actualSpeed;
    private final double physicalRadius;
    public static final double SENSORY_RADIUS = 120.0;

    /**
     * Constructs a new Creature with a designated name, genome, and starting coordinates.
     *
     * @param name   Designation name (e.g. "Apex-A")
     * @param x      Initial X position
     * @param y      Initial Y position
     * @param genome Genetic configuration
     * @param rng    Random instance for initial angle
     */
    public Creature(String name, double x, double y, Genome genome, Random rng) {
        this.name = name != null ? name : "Apex-Unknown";
        this.x = x;
        this.y = y;
        this.genome = genome;
        this.foodEaten = 0;
        this.survivalTicks = 0;
        this.alive = true;

        // Map Genome traits (5.0 - 100.0) to physical attributes
        // Speed: 1.2 to 5.8 pixels/tick
        this.actualSpeed = 1.0 + (genome.getSpeed() / 100.0) * 4.8;

        // Size: 3.5 to 15.0 radius in pixels
        this.physicalRadius = 3.5 + (genome.getSize() / 100.0) * 11.5;

        // Max energy capacity scales with creature size (bigger creatures store more fat/energy)
        this.maxEnergy = 100.0 + (genome.getSize() / 100.0) * 150.0;
        this.energy = this.maxEnergy * 0.85; // Start with 85% full energy

        this.wanderAngle = rng.nextDouble() * Math.PI * 2.0;
        this.vx = Math.cos(wanderAngle) * actualSpeed;
        this.vy = Math.sin(wanderAngle) * actualSpeed;
    }

    public Creature(double x, double y, Genome genome, Random rng) {
        this("Apex-0", x, y, genome, rng);
    }

    /**
     * Executes one simulation tick for this creature:
     * 1. Consumes metabolic energy (BMR) and checks for starvation.
     * 2. Queries QuadTree for nearby food within sensory radius in O(log N).
     * 3. Steers towards closest food or executes wandering behavior.
     * 4. Eats reachable food and updates position.
     *
     * @param foodTree    Spatial QuadTree containing active food items
     * @param arenaWidth  Width of the simulation arena
     * @param arenaHeight Height of the simulation arena
     * @param rng         Random instance
     */
    public void update(QuadTree<Food> foodTree, double arenaWidth, double arenaHeight, Random rng) {
        if (!alive) {
            return;
        }

        survivalTicks++;

        // Real-time metabolic energy decay
        double bmr = genome.calculateBMR();
        energy -= bmr;

        if (energy <= 0) {
            energy = 0;
            alive = false;
            return;
        }

        // QuadTree Spatial Query for food within sensory radius (O(log N))
        List<Food> visibleFoods = new ArrayList<>(16);
        foodTree.queryCircle(x, y, SENSORY_RADIUS, visibleFoods);

        Food nearestFood = null;
        double minDistanceSq = Double.MAX_VALUE;

        for (Food food : visibleFoods) {
            if (!food.isEaten()) {
                double dx = food.getX() - x;
                double dy = food.getY() - y;
                double distSq = dx * dx + dy * dy;
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    nearestFood = food;
                }
            }
        }

        if (nearestFood != null) {
            // Vector steering towards target food
            double dx = nearestFood.getX() - x;
            double dy = nearestFood.getY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 0.001) {
                double targetVx = (dx / dist) * actualSpeed;
                double targetVy = (dy / dist) * actualSpeed;

                // Smooth steering interpolation (turn rate)
                double steerFactor = 0.25;
                vx = vx * (1.0 - steerFactor) + targetVx * steerFactor;
                vy = vy * (1.0 - steerFactor) + targetVy * steerFactor;

                // Re-normalize velocity to actualSpeed
                double currentSpeed = Math.sqrt(vx * vx + vy * vy);
                if (currentSpeed > 0.001) {
                    vx = (vx / currentSpeed) * actualSpeed;
                    vy = (vy / currentSpeed) * actualSpeed;
                }
                wanderAngle = Math.atan2(vy, vx);
            }

            // Check if within eating reach
            double eatThreshold = physicalRadius + nearestFood.getRadius();
            if (dist <= eatThreshold && !nearestFood.isEaten()) {
                nearestFood.setEaten(true);
                foodEaten++;
                // Digest food: strength provides slight digestion efficiency bonus
                double digestionEfficiency = 1.0 + (genome.getStrength() / 100.0) * 0.25;
                energy = Math.min(maxEnergy, energy + nearestFood.getEnergyValue() * digestionEfficiency);
            }
        } else {
            // Random walk / wandering behavior with smooth steering changes
            wanderAngle += (rng.nextDouble() - 0.5) * 0.4;
            double targetVx = Math.cos(wanderAngle) * actualSpeed;
            double targetVy = Math.sin(wanderAngle) * actualSpeed;

            double steerFactor = 0.15;
            vx = vx * (1.0 - steerFactor) + targetVx * steerFactor;
            vy = vy * (1.0 - steerFactor) + targetVy * steerFactor;

            double currentSpeed = Math.sqrt(vx * vx + vy * vy);
            if (currentSpeed > 0.001) {
                vx = (vx / currentSpeed) * actualSpeed;
                vy = (vy / currentSpeed) * actualSpeed;
            }
        }

        // Boundary handling: soft repulsion from walls
        double margin = 30.0;
        if (x < margin) {
            vx += (margin - x) * 0.1;
        } else if (x > arenaWidth - margin) {
            vx -= (x - (arenaWidth - margin)) * 0.1;
        }

        if (y < margin) {
            vy += (margin - y) * 0.1;
        } else if (y > arenaHeight - margin) {
            vy -= (y - (arenaHeight - margin)) * 0.1;
        }

        // Update position
        x += vx;
        y += vy;

        // Hard clamping to arena bounds
        x = Math.max(physicalRadius, Math.min(arenaWidth - physicalRadius, x));
        y = Math.max(physicalRadius, Math.min(arenaHeight - physicalRadius, y));
    }

    /**
     * Calculates the fitness score of this creature for natural selection.
     * Weighted primarily by food consumed (reproductive viability), survival longevity, and remaining energy.
     *
     * @return Calculated fitness score
     */
    public double getFitness() {
        double foodScore = foodEaten * 160.0;
        double survivalScore = survivalTicks * 0.35;
        double energyBonus = alive ? (energy / maxEnergy) * 40.0 : 0.0;
        return Math.max(0.01, foodScore + survivalScore + energyBonus);
    }

    /**
     * Natural ordering for sorting / priority queue:
     * Compares based on fitness in ascending order.
     */
    @Override
    public int compareTo(Creature other) {
        return Double.compare(this.getFitness(), other.getFitness());
    }

    // PointItem implementation
    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    // Getters
    public String getName() {
        return name;
    }

    public Genome getGenome() {
        return genome;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getEnergy() {
        return energy;
    }

    public double getMaxEnergy() {
        return maxEnergy;
    }

    public int getFoodEaten() {
        return foodEaten;
    }

    public int getSurvivalTicks() {
        return survivalTicks;
    }

    public boolean isAlive() {
        return alive;
    }

    public double getActualSpeed() {
        return actualSpeed;
    }

    public double getPhysicalRadius() {
        return physicalRadius;
    }

    public double getEnergyPercentage() {
        return Math.max(0.0, Math.min(1.0, energy / maxEnergy));
    }
}
