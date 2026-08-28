package com.evolution.model;

import java.util.Random;

/**
 * Represents the genetic code of an individual creature.
 * Controls physical traits: speed, size, and strength.
 * All traits are clamped between 5.0 and 100.0.
 */
public class Genome {
    public static final double MIN_TRAIT_VALUE = 5.0;
    public static final double MAX_TRAIT_VALUE = 100.0;

    private double speed;
    private double size;
    private double strength;

    /**
     * Constructs a Genome with explicit trait values.
     * Values are clamped to [MIN_TRAIT_VALUE, MAX_TRAIT_VALUE].
     *
     * @param speed    Movement rate trait
     * @param size     Physical body dimension trait
     * @param strength Competitive / power trait
     */
    public Genome(double speed, double size, double strength) {
        this.speed = clamp(speed);
        this.size = clamp(size);
        this.strength = clamp(strength);
    }

    /**
     * Copy constructor.
     *
     * @param other Source genome to duplicate
     */
    public Genome(Genome other) {
        this(other.speed, other.size, other.strength);
    }

    /**
     * Generates a random genome with uniformly distributed traits.
     *
     * @param rng Random number generator instance
     * @return A newly randomized Genome
     */
    public static Genome createRandom(Random rng) {
        return new Genome(
            MIN_TRAIT_VALUE + rng.nextDouble() * (MAX_TRAIT_VALUE - MIN_TRAIT_VALUE),
            MIN_TRAIT_VALUE + rng.nextDouble() * (MAX_TRAIT_VALUE - MIN_TRAIT_VALUE),
            MIN_TRAIT_VALUE + rng.nextDouble() * (MAX_TRAIT_VALUE - MIN_TRAIT_VALUE)
        );
    }

    /**
     * Calculates the Basal Metabolic Rate (BMR) energy cost per tick.
     * Demonstrates non-linear evolutionary trade-offs:
     * - Speed incurs kinetic drag cost (speed^1.4)
     * - Size incurs cubic/mass volume cost (size^1.6)
     * - Strength incurs muscular maintenance cost
     *
     * @return Energy consumption value per simulation tick
     */
    public double calculateBMR() {
        double baseCost = 0.12;
        double speedCost = Math.pow(speed, 1.4) * 0.0035;
        double sizeCost = Math.pow(size, 1.6) * 0.0025;
        double strengthCost = strength * 0.0018;

        return baseCost + speedCost + sizeCost + strengthCost;
    }

    /**
     * Performs Uniform Crossover with a second parent genome.
     * Each gene is randomly selected from either parent with equal 50% probability.
     *
     * @param partner The second parent genome
     * @param rng     Random number generator instance
     * @return A new recombinant Genome
     */
    public Genome crossoverUniform(Genome partner, Random rng) {
        double childSpeed = rng.nextBoolean() ? this.speed : partner.speed;
        double childSize = rng.nextBoolean() ? this.size : partner.size;
        double childStrength = rng.nextBoolean() ? this.strength : partner.strength;
        return new Genome(childSpeed, childSize, childStrength);
    }

    /**
     * Performs Two-Point Crossover across the ordered gene vector [speed, size, strength].
     *
     * @param partner The second parent genome
     * @param rng     Random number generator instance
     * @return A new recombinant Genome
     */
    public Genome crossoverTwoPoint(Genome partner, Random rng) {
        double[] p1 = {this.speed, this.size, this.strength};
        double[] p2 = {partner.speed, partner.size, partner.strength};
        double[] child = new double[3];

        int point1 = rng.nextInt(3);
        int point2 = rng.nextInt(3);
        int start = Math.min(point1, point2);
        int end = Math.max(point1, point2);

        for (int i = 0; i < 3; i++) {
            if (i >= start && i <= end) {
                child[i] = p2[i];
            } else {
                child[i] = p1[i];
            }
        }

        return new Genome(child[0], child[1], child[2]);
    }

    /**
     * Default Crossover method (blends uniform and two-point genetic recombination).
     *
     * @param partner The second parent genome
     * @param rng     Random number generator instance
     * @return Recombinant Genome
     */
    public Genome crossover(Genome partner, Random rng) {
        if (rng.nextBoolean()) {
            return crossoverUniform(partner, rng);
        } else {
            return crossoverTwoPoint(partner, rng);
        }
    }

    /**
     * Applies Gaussian Mutation to each gene based on the mutation rate and scale.
     * Perturbed values are clamped to preserve valid ranges.
     *
     * @param mutationRate  Probability [0.0, 1.0] of any given gene mutating
     * @param mutationScale Standard deviation / intensity of Gaussian perturbation
     * @param rng           Random number generator instance
     * @return This mutated genome for chaining
     */
    public Genome mutate(double mutationRate, double mutationScale, Random rng) {
        if (rng.nextDouble() < mutationRate) {
            this.speed = clamp(this.speed + rng.nextGaussian() * mutationScale);
        }
        if (rng.nextDouble() < mutationRate) {
            this.size = clamp(this.size + rng.nextGaussian() * mutationScale);
        }
        if (rng.nextDouble() < mutationRate) {
            this.strength = clamp(this.strength + rng.nextGaussian() * mutationScale);
        }
        return this;
    }

    /**
     * Clamps a value between MIN_TRAIT_VALUE and MAX_TRAIT_VALUE.
     */
    private static double clamp(double value) {
        return Math.max(MIN_TRAIT_VALUE, Math.min(MAX_TRAIT_VALUE, value));
    }

    // Getters and Setters
    public double getSpeed() {
        return speed;
    }

    public double getSize() {
        return size;
    }

    public double getStrength() {
        return strength;
    }

    public void setSpeed(double speed) {
        this.speed = clamp(speed);
    }

    public void setSize(double size) {
        this.size = clamp(size);
    }

    public void setStrength(double strength) {
        this.strength = clamp(strength);
    }

    @Override
    public String toString() {
        return String.format("Genome[Spd=%.1f, Siz=%.1f, Str=%.1f, BMR=%.2f]",
                speed, size, strength, calculateBMR());
    }
}
